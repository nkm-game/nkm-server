# Documentation Setup

This directory contains the MkDocs documentation for the NKM Game Server project.

## Prerequisites

- Python 3.7+
- pip (Python package installer)

## Setup

1. Install MkDocs and dependencies:

   ```bash
   pip install -r requirements.txt
   ```

2. Serve the documentation locally:

   ```bash
   mkdocs serve
   ```

   The documentation will be available at `http://127.0.0.1:8000`

## Building

To build the static site:

```bash
mkdocs build
```

The built site will be in the `site/` directory.

## Deployment

### GitHub Pages

To deploy to GitHub Pages using Mike for versioning:

```bash
# Deploy current version as 'latest'
mike deploy --push --update-aliases latest

# Deploy a specific version
mike deploy --push --update-aliases v1.0 latest
```

### Manual Deployment

```bash
# Build the site
mkdocs build

# Deploy the contents of site/ directory to your web server
```

## Adding New Documentation

1. Create new `.md` files in the appropriate subdirectory
2. Add the new pages to the `nav` section in `mkdocs.yml`
3. Test locally with `mkdocs serve`
4. Commit and push your changes

## Documentation Structure

```
docs/
├── index.md                     # Homepage
├── game-rules.md               # Game rules and mechanics
├── backend_development/        # Backend development guides
├── frontend_development/       # Frontend development guides
├── img/                        # Images and assets
├── requirements.txt            # Python dependencies
└── README.md                   # This file
```

## Tips

- Use admonitions (`!!! note`, `!!! tip`, `!!! warning`) for callouts
- Include code blocks with syntax highlighting
- Use the Material theme features like tabs and buttons
- Keep navigation structure logical and not too deep
- Add images to the `img/` directory and reference them relatively
