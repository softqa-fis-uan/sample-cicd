#!/usr/bin/env python3
"""Turn Checkstyle, PMD and SpotBugs XML reports into GitHub annotations."""

import os
import xml.etree.ElementTree as ET
from pathlib import Path

WORKSPACE = Path(os.environ.get("GITHUB_WORKSPACE", ".")).resolve()
REPORTS = Path("java/build/reports")
MAIN_SOURCES = "java/src/main/java"


def load(report: Path) -> ET.Element:
    root = ET.parse(report).getroot()
    for element in root.iter():  # drop XML namespaces (PMD uses one)
        element.tag = element.tag.rsplit("}", 1)[-1]
    return root


def emit(level: str, file: str, line: str | None, title: str, message: str) -> None:
    path = Path(file)
    if path.is_absolute() and WORKSPACE in path.parents:
        path = path.relative_to(WORKSPACE)
    message = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    print(f"::{level} file={path},line={line or 1},title={title}::{message}")


def checkstyle(report: Path) -> None:
    for file in load(report).iter("file"):
        for error in file.iter("error"):
            level = "warning" if error.get("severity") == "warning" else "error"
            rule = error.get("source", "").rsplit(".", 1)[-1]
            emit(
                level,
                file.get("name"),
                error.get("line"),
                f"Checkstyle: {rule}",
                error.get("message", ""),
            )


def pmd(report: Path) -> None:
    for file in load(report).iter("file"):
        for violation in file.iter("violation"):
            emit(
                "error",
                file.get("name"),
                violation.get("beginline"),
                f"PMD: {violation.get('rule')}",
                (violation.text or "").strip(),
            )


def spotbugs(report: Path) -> None:
    for bug in load(report).iter("BugInstance"):
        source = next((s for s in bug.iter("SourceLine") if s.get("start")), None)
        if source is None:
            continue
        message = (bug.findtext("LongMessage") or bug.get("type") or "").strip()
        emit(
            "error",
            f"{MAIN_SOURCES}/{source.get('sourcepath')}",
            source.get("start"),
            f"SpotBugs: {bug.get('type')}",
            message,
        )


def main() -> None:
    for report in sorted((REPORTS / "checkstyle").glob("*.xml")):
        checkstyle(report)
    for report, handler in (
        (REPORTS / "pmd" / "main.xml", pmd),
        (REPORTS / "spotbugs" / "main.xml", spotbugs),
    ):
        if report.exists():
            handler(report)


if __name__ == "__main__":
    main()
