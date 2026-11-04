---
name: EduTrack Attendance
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#444651'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#757682'
  outline-variant: '#c5c5d3'
  surface-tint: '#4059aa'
  primary: '#00236f'
  on-primary: '#ffffff'
  primary-container: '#1e3a8a'
  on-primary-container: '#90a8ff'
  inverse-primary: '#b6c4ff'
  secondary: '#0051d5'
  on-secondary: '#ffffff'
  secondary-container: '#316bf3'
  on-secondary-container: '#fefcff'
  tertiary: '#002e41'
  on-tertiary: '#ffffff'
  tertiary-container: '#00455f'
  on-tertiary-container: '#2eb7f2'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce1ff'
  primary-fixed-dim: '#b6c4ff'
  on-primary-fixed: '#00164e'
  on-primary-fixed-variant: '#264191'
  secondary-fixed: '#dbe1ff'
  secondary-fixed-dim: '#b4c5ff'
  on-secondary-fixed: '#00174b'
  on-secondary-fixed-variant: '#003ea8'
  tertiary-fixed: '#c4e7ff'
  tertiary-fixed-dim: '#7bd0ff'
  on-tertiary-fixed: '#001e2c'
  on-tertiary-fixed-variant: '#004c69'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.03em
  data-mono:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 0.75rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes an authoritative, reliable, and frictionless environment tailored for educational institutions, administrators, educators, and academic operations staff. The visual identity communicates structural precision and institutional trust without feeling bureaucratic or legacy-bound.

The visual execution combines **Corporate / Modern** discipline with high-legibility utilitarian clarity:
- **Tone:** Authoritative, dependable, calming, and exceptionally organized.
- **Atmosphere:** Clean slate surfaces, clinical separation of data rows, deliberate status indicators, and zero decorative noise.
- **Focus:** Fast, error-free input loops (batch check-ins, rapid status flipping) and high-level analytical visibility (retention rates, absent spikes, compliance metrics).

## Colors

The palette leverages high-contrast, predictable chromatic signaling engineered for clarity during high-velocity data verification:

- **Primary & Interactive Tiers:**
  - `#1E3A8A` (Deep Institutional Indigo): Anchor headers, primary navigation sidebars, structural frame accents, and high-emphasis brand marks.
  - `#2563EB` (Vibrant Cobalt): Primary interactive buttons, selected states, active tab markers, and focus indicators.
  - `#38BDF8` (Sky Blue): Metric highlights, neutral attendance states (Excused/Pending), and subtle progress ring indicators.

- **Status & Semantic Signals:**
  - **Present:** `#10B981` (Emerald). Applied to confirmed statuses, high attendance rates (>90%), and confirmation alerts. Paired with soft tint `#ECFDF5`.
  - **Late / Tardy:** `#F59E0B` (Amber). Applied to grace period entries, partial attendance, and warning triggers. Paired with soft tint `#FFFBEB`.
  - **Absent:** `#EF4444` (Rose Red). Critical flags, unexcused non-attendance, and threshold dropouts. Paired with soft tint `#FEF2F2`.

- **Surfaces & Structural Neutrals:**
  - **Canvas Background:** `#F8FAFC` (Slate 50).
  - **Card & Surface Layers:** `#FFFFFF` (Pure White).
  - **Borders & Dividers:** `#E2E8F0` (Slate 200) for structural bounding; `#CBD5E1` (Slate 300) for interactive control borders.
  - **Text Neutral:** `#0F172A` (Slate 900) for primary copy; `#475569` (Slate 600) for secondary details; `#94A3B8` (Slate 400) for placeholders and deactivated indicators.

## Typography

The typography pairing uses **Plus Jakarta Sans** for display and section headers to inject geometric modernism and institutional credibility, while **Inter** governs all operational UI, data tables, metrics, and interactive surfaces.

- **Numerical & Data Rendering:** Tabular numbers (`tnum`) and slashed zeros must be enabled globally across all data tables, rosters, roll-call percentages, and timestamp records to maintain strict columnar vertical alignment.
- **Hierarchy Rules:** Table headers rely strictly on `label-sm` in all-caps or title-case with light letter-spacing (`0.03em`) and neutral slate coloring (`#64748B`). Hero rates and roll statistics map to `headline-xl` paired directly with `label-md` tracking descriptors.

## Layout & Spacing

The layout model is anchored by a fixed-sidebar, fluid-canvas responsive grid designed for screen-dense information displays:

- **Grid Architecture:** 12-column responsive layout on desktop viewports (min-width: 1024px) with `1.5rem` gutters and outer canvas margins of `2rem`. Collapses to a 4-column layout with `0.75rem` gutters and `1rem` margins on mobile devices.
- **Application Shell:** 
  - Desktop: Persistent primary navigation rail (240px wide) with flexible data viewport.
  - Mobile & Tablet: Collapsed bottom bar or slide-over drawer with fixed app-header for roster filtering.
- **Table Density Profiles:**
  - *Standard Mode:* 48px row heights, `space-md` cell padding.
  - *Compact / Bulk Mode:* 36px row heights, `space-sm` cell padding for swift roll-call entry during class transitions.

## Elevation & Depth

Visual hierarchy relies on low-contrast structural boundaries reinforced by soft, cool-tinted ambient drop shadows:

- **Level 0 (Flat / Canvas):** Applied to the main page canvas (`#F8FAFC`). No shadow.
- **Level 1 (Card & Content Blocks):** Pure white background (`#FFFFFF`) framed by a hairline 1px solid border (`#E2E8F0`) and an ambient shadow: `0 1px 3px 0 rgba(15, 23, 42, 0.05), 0 1px 2px -1px rgba(15, 23, 42, 0.03)`.
- **Level 2 (Dropdowns, Popovers & Hover Cards):** White background, border `#CBD5E1`, with shadow: `0 4px 6px -1px rgba(15, 23, 42, 0.08), 0 2px 4px -2px rgba(15, 23, 42, 0.04)`.
- **Level 3 (Modal Dialogs & Slide-out Roster Drawers):** White background with elevation shadow: `0 20px 25px -5px rgba(15, 23, 42, 0.1), 0 8px 10px -6px rgba(15, 23, 42, 0.05)`.
- **Active Focus Ring:** `0 0 0 3px rgba(37, 99, 235, 0.25)` applied over a crisp 1px `#2563EB` boundary.

## Shapes

The design system enforces a **Rounded (Level 2)** shape standard:

- **Standard Controls (Inputs, Buttons, Badges):** `0.5rem` (8px) radius to maintain an approachable yet structured profile.
- **Cards, Modals & Data Tables:** `rounded-lg` (`1rem` / 16px) outer boundary corners; internal table elements inherit square geometry along row edges.
- **Chips, Pill Toggles & Status Indicators:** Retain a fully rounded geometry (`9999px`) to create distinction between container boxes and micro-status tags.
- **Percentage Rings & Circular Avatars:** Strictly circular (1:1 aspect ratio with 50% border radius).

## Components

### Buttons
- **Primary:** Solid `#2563EB`, text `#FFFFFF`, hover `#1D4ED8`, active `#1E40AF`.
- **Secondary / Outline:** `#FFFFFF` background, 1px border `#CBD5E1`, text `#0F172A`. Hover: `#F1F5F9`.
- **Status Fast-Action Buttons:** Compact three-segment micro-buttons per table row for "P" (Present - Green hover), "L" (Late - Amber hover), and "A" (Absent - Red hover).

### Status Badges & Chips
- Fully rounded pills (`9999px`) with 12px height text (`label-md`), featuring a 6px solid circular dot preceding the text:
  - **Present:** `#ECFDF5` background, `#065F46` text, `#10B981` status dot.
  - **Late:** `#FFFBEB` background, `#92400E` text, `#F59E0B` status dot.
  - **Absent:** `#FEF2F2` background, `#991B1B` text, `#EF4444` status dot.
  - **Excused:** `#F0F9FF` background, `#075985` text, `#38BDF8` status dot.

### Segmented Controls
- Contained slate track (`#F1F5F9`) with 4px padding. Selected segment receives a white elevated pill (`#FFFFFF`), crisp hairline border (`#E2E8F0`), subtle shadow, and `#0F172A` text weight 600. Unselected items use `#64748B` with transparent background.

### Stat Cards with Percentage Rings
- White surface, `1rem` border radius, bordered in `#E2E8F0`. Features a metric headline (`headline-lg`), a subtitle description, and an integrated SVG progress ring:
  - Background track: `#E2E8F0` with 4px stroke width.
  - Progress stroke: Dynamic color based on metric health (`#10B981`, `#F59E0B`, or `#EF4444`) with rounded line-caps. Center of ring displays percentage in `data-mono` (weight 700).

### Form Fields & Filters
- Clean inputs with height of 40px (desktop) / 44px (mobile touch targets).
- Slate border `#CBD5E1`, white fill, `#0F172A` text, `#94A3B8` placeholder.
- Focus state activates `#2563EB` border with a 3px matching glow ring. Dropdown selects feature refined down-chevron icons embedded right.

### Responsive Data Tables & Pagination
- **Table Container:** Full-width card with overflow-x scrolling enabled. Row dividers use 1px `#E2E8F0`. 
- **Header:** Sticky `#F8FAFC` top row with subtle bottom border, uppercase tracking labels (`label-sm`).
- **Row States:** Default white; hover state `#F8FAFC`. Selected rows for batch actions show a `#EFF6FF` tint with a left-accent border in `#2563EB`.
- **Pagination Footer:** Border-top `#E2E8F0`, containing item counts ("Showing 1–25 of 142 students"), items-per-page dropdown, and segmented page buttons with previous/next arrows.