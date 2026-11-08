#!/usr/bin/env python3
"""가짜 차. 폰 앱을 차량 앱 없이 검증한다 (plan/work-plan.md 2.4절 T2).

폰(기본 127.0.0.1:8080, 폰 에뮬레이터면 콘솔 redir 로 8080 을 넘긴 뒤)에 접속해 HELLO 를 보내고,
받은 FRAME 의 크기·fps, LIGHT, HELLO 를 찍는다. stdin 으로 차 쪽 LIGHT 를 보낼 수 있다:
    k 3500 | l 0.6 | p cyan|none | q
--save 를 주면 마지막 FRAME 을 그 경로에 JPEG 로 저장한다(크기 확인용). 표준 라이브러리만 쓴다.
"""

import argparse
import json
import socket
import struct
import sys
import threading
import time

TYPE_HELLO, TYPE_FRAME, TYPE_LIGHT, TYPE_PING = 1, 2, 3, 4
NAMES = {1: "HELLO", 2: "FRAME", 3: "LIGHT", 4: "PING"}
MAX = {TYPE_FRAME: 1 << 20, TYPE_LIGHT: 4 << 10}
PING_INTERVAL = 5.0


def write_msg(sock, mtype, payload=b""):
    sock.sendall(struct.pack(">BI", mtype, len(payload)) + payload)


def read_exact(sock, n):
    buf = b""
    while len(buf) < n:
        chunk = sock.recv(n - len(buf))
        if not chunk:
            return None
        buf += chunk
    return buf


def read_msg(sock):
    head = read_exact(sock, 5)
    if head is None:
        return None
    mtype, length = struct.unpack(">BI", head)
    if length > MAX.get(mtype, 4 << 10):
        raise IOError(f"{NAMES.get(mtype, mtype)} length {length} 상한 초과")
    payload = read_exact(sock, length) if length else b""
    if payload is None:
        return None
    return mtype, payload


def jpeg_size(data):
    """SOF 마커에서 폭·높이. 못 읽으면 None."""
    i = 2
    while i + 9 < len(data):
        if data[i] != 0xFF:
            return None
        marker = data[i + 1]
        if marker in (0xC0, 0xC1, 0xC2):
            h, w = struct.unpack(">HH", data[i + 5:i + 9])
            return w, h
        seg = struct.unpack(">H", data[i + 2:i + 4])[0]
        i += 2 + seg
    return None


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", type=int, default=8080)
    ap.add_argument("--save", help="마지막 FRAME 을 저장할 JPEG 경로")
    args = ap.parse_args()

    sock = socket.create_connection((args.host, args.port), timeout=5)
    sock.settimeout(None)
    sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
    print(f"[mock_car] {args.host}:{args.port} 접속", flush=True)
    wlock = threading.Lock()
    stop = threading.Event()
    state = {"kelvin": 4000, "level": 0.6, "preset": None}

    def send(mtype, payload=b""):
        with wlock:
            write_msg(sock, mtype, payload)

    def send_light():
        send(TYPE_LIGHT, json.dumps({"v": 1, **state, "src": "car", "ts": int(time.time() * 1000)}).encode())

    def pings():
        while not stop.wait(PING_INTERVAL):
            try:
                send(TYPE_PING)
            except OSError:
                return

    def stdin_loop():
        for line in sys.stdin:
            parts = line.split()
            if not parts:
                continue
            if parts[0] == "k" and len(parts) > 1:
                state["kelvin"], state["preset"] = int(parts[1]), None
            elif parts[0] == "l" and len(parts) > 1:
                state["level"] = float(parts[1])
            elif parts[0] == "p" and len(parts) > 1:
                state["preset"] = None if parts[1] == "none" else parts[1]
            elif parts[0] == "q":
                stop.set()
                sock.close()
                return
            else:
                print("명령: k <켈빈> | l <0..1> | p <cyan|blue|purple|none> | q", flush=True)
                continue
            try:
                send_light()
            except OSError:
                return

    send(TYPE_HELLO, json.dumps({"v": 1, "role": "car", "screen": [1680, 1210], "now": int(time.time() * 1000)}).encode())
    threading.Thread(target=pings, daemon=True).start()
    threading.Thread(target=stdin_loop, daemon=True).start()

    frames, bytes_total, t0, last = 0, 0, time.time(), None
    try:
        while True:
            m = read_msg(sock)
            if m is None:
                print("[mock_car] 폰이 끊음", flush=True)
                break
            mtype, payload = m
            if mtype == TYPE_FRAME:
                frames += 1
                bytes_total += len(payload)
                last = payload
                now = time.time()
                if now - t0 >= 2.0:
                    print(f"[mock_car] FRAME {frames / (now - t0):.1f} fps, 평균 {bytes_total // max(frames, 1) // 1024} KB, 크기 {jpeg_size(payload)}", flush=True)
                    frames, bytes_total, t0 = 0, 0, now
            elif mtype == TYPE_LIGHT:
                print(f"[mock_car] 폰 → LIGHT {payload.decode(errors='replace')}", flush=True)
            elif mtype == TYPE_HELLO:
                print(f"[mock_car] 폰 → HELLO {payload.decode(errors='replace')}", flush=True)
    except (OSError, IOError) as e:
        if not stop.is_set():
            print(f"[mock_car] 연결 오류: {e}", flush=True)
    finally:
        stop.set()
        sock.close()
        if args.save and last:
            open(args.save, "wb").write(last)
            print(f"[mock_car] 마지막 FRAME 저장: {args.save} ({len(last)}B, {jpeg_size(last)})", flush=True)


if __name__ == "__main__":
    main()
