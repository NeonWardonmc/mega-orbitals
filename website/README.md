# Documentation website

The site (https://mega-orbitals.ishowavoid.dev, on GitHub Pages) is built from:

- `docs/`: the guides (they also read fine on GitHub)
- `CANNONS.md`, `CHANGELOG.md`, `CONTRIBUTING.md` from the project root
- `website/index.md`: the website's home page, and `website/assets/` (styles, icon)
- `mkdocs.yml`: site settings, menu and theme

`tools/prepare_site.py` copies all of that into `.site-src/` and fixes the links for the website. MkDocs then builds the site into `.site-build/`.
GitHub Actions (`.github/workflows/docs.yml`) does this and publishes it on every push to `main`.

## Preview it on your computer

```
python -m pip install -r website/requirements.txt
python tools/prepare_site.py
python -m mkdocs serve
```

Then open http://127.0.0.1:8000. Re-run `prepare_site.py` after editing a page.
