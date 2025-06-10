# Theme Color Options

This page shows popular, well-established color combinations for the Material theme that are widely used across documentation sites.

## Current Theme: Deep Purple + Amber

We're currently using **deep purple** as the primary color and **amber** as the accent color. This is a popular combination for developer tools and technical documentation that provides excellent contrast in both light and dark modes.

## Popular Alternatives

Here are some widely-used color combinations that don't reinvent the wheel:

### GitHub Style

```yaml
primary: indigo
accent: indigo
```

Classic GitHub-inspired colors. Very professional and widely recognized.

### Documentation Classic

```yaml
primary: blue
accent: amber
```

Traditional documentation colors used by many tech companies.

### Modern Minimal

```yaml
primary: grey
accent: cyan
```

Clean, modern look popular with minimalist designs.

### Corporate Blue

```yaml
primary: blue
accent: light blue
```

Professional blue theme commonly used in corporate documentation.

### Developer Friendly

```yaml
primary: deep purple
accent: amber
```

Popular among developer tools and technical documentation.

### Green Tech

```yaml
primary: green
accent: light green
```

Often used for environmental or growth-focused tech projects.

### Red Alert

```yaml
primary: red
accent: pink
```

Bold and attention-grabbing, good for tools or alerts.

## How to Change

To change the color scheme, edit `mkdocs.yml` and update the `primary` and `accent` values:

```yaml
theme:
  palette:
    # Palette toggle for light mode
    - media: "(prefers-color-scheme: light)"
      scheme: default
      primary: [COLOR_NAME] # Change this
      accent: [COLOR_NAME] # Change this
      toggle:
        icon: material/brightness-7
        name: Switch to dark mode

    # Palette toggle for dark mode
    - media: "(prefers-color-scheme: dark)"
      scheme: slate
      primary: [COLOR_NAME] # Change this
      accent: [COLOR_NAME] # Change this
      toggle:
        icon: material/brightness-4
        name: Switch to system preference
```

## Available Colors

All available Material Design colors:
`red`, `pink`, `purple`, `deep purple`, `indigo`, `blue`, `light blue`, `cyan`, `teal`, `green`, `light green`, `lime`, `yellow`, `amber`, `orange`, `deep orange`, `brown`, `grey`, `blue grey`, `black`, `white`

## Testing

After making changes:

1. Run `mkdocs serve` to preview locally
2. Check both light and dark modes
3. Ensure good contrast and readability
