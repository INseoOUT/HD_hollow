"""Gradle 단위 테스트 결과(모듈별 build/test-results/testDebugUnitTest/*.xml)를 junit 파일 하나로 합친다.

Sprintify 엔진은 /out/junit.xml 하나만 읽는다(parse_junit). 테스트 이름의 `[T-042/1]` 태그가 수용 기준 표식이다.
사용: python3 merge_junit.py <gradle 루트> <출력 경로>
"""

import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def main(root: str, out: str) -> int:
    suites = ET.Element("testsuites")
    files = sorted(Path(root).glob("*/build/test-results/testDebugUnitTest/*.xml"))
    for f in files:
        node = ET.parse(f).getroot()
        if node.tag == "testsuite":
            suites.append(node)
        else:
            suites.extend(list(node.iter("testsuite")))
    Path(out).parent.mkdir(parents=True, exist_ok=True)
    ET.ElementTree(suites).write(out, encoding="utf-8", xml_declaration=True)
    cases = sum(1 for _ in suites.iter("testcase"))
    print(f"junit: {len(files)}개 파일, 테스트 {cases}개 → {out}")
    return 0


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit("사용: merge_junit.py <gradle 루트> <출력 경로>")
    sys.exit(main(sys.argv[1], sys.argv[2]))
