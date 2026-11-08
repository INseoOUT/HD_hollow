#!/usr/bin/env python3
"""가짜 폰. 차량 앱을 폰 앱 없이 검증한다 (plan/work-plan.md 2.4절 T1).

8080 에서 기다리다 차가 붙으면 HELLO 를 보내고, --jpeg 가 있으면 그 파일을 15fps 로 FRAME 으로 반복 송신한다.
--sweep 이면 1초마다 색온도를 2700→8000K 로 올리는 LIGHT 를 보낸다. stdin 으로도 보낼 수 있다:
    k 3500      색온도
    l 0.6       밝기
    p cyan      무드 색 (none 이면 색온도 모드)
    q           종료
차에서 온 LIGHT 와 HELLO 는 그대로 찍는다. 표준 라이브러리만 쓴다.

에뮬레이터: 차량 에뮬레이터 안에서 Mac 은 10.0.2.2 이므로 차량 앱의 기본 주소(10.0.2.2:8080)로 이 서버에 닿는다.
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


class State:
    def __init__(self):
        self.kelvin, self.level, self.preset = 4000, 0.6, None
        self.lock = threading.Lock()

    def light_json(self):
        with self.lock:
            return json.dumps({"v": 1, "kelvin": self.kelvin, "level": self.level, "preset": self.preset,
                               "src": "phone", "ts": int(time.time() * 1000)}).encode()


def serve_client(sock, addr, state, jpeg, sweep):
    print(f"[mock_phone] 차 접속 {addr}", flush=True)
    stop = threading.Event()
    wlock = threading.Lock()

    def send(mtype, payload=b""):
        with wlock:
            write_msg(sock, mtype, payload)

    def send_light():
        send(TYPE_LIGHT, state.light_json())

    def frames():
        interval = 1 / 15
        while not stop.is_set():
            try:
                send(TYPE_FRAME, jpeg)
            except OSError:
                return
            time.sleep(interval)

    def pings():
        while not stop.wait(PING_INTERVAL):
            try:
                send(TYPE_PING)
            except OSError:
                return

    def sweeper():
        k = 2700
        while not stop.wait(1.0):
            with state.lock:
                state.kelvin, state.preset = k, None
            try:
                send_light()
            except OSError:
                return
            k = 2700 if k >= 8000 else k + 100

    def stdin_loop():
        for line in sys.stdin:
            parts = line.split()
            if not parts:
                continue
            cmd = parts[0]
            with state.lock:
                if cmd == "k" and len(parts) > 1:
                    state.kelvin, state.preset = int(parts[1]), None
                elif cmd == "l" and len(parts) > 1:
                    state.level = float(parts[1])
                elif cmd == "p" and len(parts) > 1:
                    state.preset = None if parts[1] == "none" else parts[1]
                elif cmd == "q":
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

    try:
        send(TYPE_HELLO, json.dumps({"v": 1, "role": "phone", "screen": [1080, 2400]}).encode())
        send_light()
        threading.Thread(target=pings, daemon=True).start()
        if jpeg:
            threading.Thread(target=frames, daemon=True).start()
        if sweep:
            threading.Thread(target=sweeper, daemon=True).start()
        threading.Thread(target=stdin_loop, daemon=True).start()
        while True:
            m = read_msg(sock)
            if m is None:
                break
            mtype, payload = m
            if mtype == TYPE_LIGHT:
                print(f"[mock_phone] 차 → LIGHT {payload.decode(errors='replace')}", flush=True)
            elif mtype == TYPE_HELLO:
                print(f"[mock_phone] 차 → HELLO {payload.decode(errors='replace')}", flush=True)
            elif mtype != TYPE_PING:
                print(f"[mock_phone] 차 → {NAMES.get(mtype, mtype)} {len(payload)}B", flush=True)
    except (OSError, IOError) as e:
        print(f"[mock_phone] 연결 오류: {e}", flush=True)
    finally:
        stop.set()
        sock.close()
        print("[mock_phone] 차 연결 끊김. 다시 기다린다", flush=True)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--port", type=int, default=8080)
    ap.add_argument("--jpeg", help="FRAME 으로 반복 송신할 JPEG 파일")
    ap.add_argument("--sweep", action="store_true", help="1초마다 색온도 스윕 LIGHT 송신")
    args = ap.parse_args()
    jpeg = open(args.jpeg, "rb").read() if args.jpeg else None
    state = State()
    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(("0.0.0.0", args.port))
    srv.listen(1)
    print(f"[mock_phone] 0.0.0.0:{args.port} 에서 차를 기다린다 (jpeg={'있음' if jpeg else '없음'}, sweep={args.sweep})", flush=True)
    while True:
        sock, addr = srv.accept()
        sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
        serve_client(sock, addr, state, jpeg, args.sweep)


if __name__ == "__main__":
    main()
