"""Prepares the documentation website sources in .site-src/ for MkDocs (see website/README.md).

The Markdown files in docs/ and the project root are written to be read on GitHub, where they link to each other
with paths like ../CANNONS.md. This script copies them into one folder and rewrites those links for the website.

Run from anywhere:  python tools/prepare_site.py
"""
import os
import re
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, ".site-src")
REPO = "https://github.com/NeonWardonmc/mega-orbitals"

# Root files that become website pages: source -> page name.
ROOT_PAGES = {"CANNONS.md": "cannons.md", "CHANGELOG.md": "changelog.md", "CONTRIBUTING.md": "contributing.md"}


def read(p):
    with open(p, encoding="utf-8") as f:
        return f.read()


def write(p, s):
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(s)


def fix_links(text, from_root):
    """Rewrites links so they work on the website. from_root = the file lived in the project root (not docs/)."""
    def repl(m):
        label, target = m.group(1), m.group(2)
        if re.match(r"^[a-z]+:", target) or target.startswith("#"):
            return m.group(0)
        path, _, anchor = target.partition("#")
        anchor = "#" + anchor if anchor else ""
        norm = os.path.normpath(os.path.join("" if from_root else "docs", path)).replace("\\", "/")
        if norm in ROOT_PAGES:
            new = ROOT_PAGES[norm]
        elif norm == "docs/README.md" or norm == "docs":
            new = "index.md"
        elif norm.startswith("docs/") and norm.endswith(".md"):
            new = norm[len("docs/"):]
        elif norm.endswith(".md") and "/" not in norm:
            new = norm  # another root file that is not a page: link to GitHub
            return f"[{label}]({REPO}/blob/main/{new}{anchor})"
        else:
            kind = "blob" if os.path.isfile(os.path.join(ROOT, norm)) else "tree"
            return f"[{label}]({REPO}/{kind}/main/{norm}{anchor})"
        return f"[{label}]({new}{anchor})"
    return re.sub(r"\[([^\]]*)\]\(([^)\s]+)\)", repl, text)


def details_to_admonitions(text):
    """GitHub's <details> blocks (in CANNONS.md) become collapsible MkDocs blocks."""
    def repl(m):
        title = (m.group(1) + m.group(2)).strip()
        lines = m.group(3).strip("\n").split("\n")
        body = "\n".join(("    " + line) if line.strip() else "" for line in lines)
        return f'??? abstract "{title}"\n\n{body}\n'
    return re.sub(r"<details><summary><b>(.*?)</b>(.*?)</summary>\n(.*?)\n</details>\n", repl, text, flags=re.S)


def main():
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    os.makedirs(OUT)
    docs = os.path.join(ROOT, "docs")
    for name in os.listdir(docs):
        if not name.endswith(".md") or name == "README.md":
            continue
        write(os.path.join(OUT, name), fix_links(read(os.path.join(docs, name)), from_root=False))
    for src, page in ROOT_PAGES.items():
        text = fix_links(read(os.path.join(ROOT, src)), from_root=True)
        if src == "CANNONS.md":
            text = details_to_admonitions(text)
        write(os.path.join(OUT, page), text)
    write(os.path.join(OUT, "index.md"), read(os.path.join(ROOT, "website", "index.md")))
    shutil.copytree(os.path.join(ROOT, "website", "assets"), os.path.join(OUT, "assets"))
    cname = os.path.join(ROOT, "website", "CNAME")
    if os.path.isfile(cname):
        shutil.copy(cname, os.path.join(OUT, "CNAME"))
    print("Prepared", len(os.listdir(OUT)), "files in", OUT)


if __name__ == "__main__":
    main()
