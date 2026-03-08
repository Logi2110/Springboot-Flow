## Original Prompt (How this diagram was requested)

1. I have created this project for learning spring boot
2. Very minimal explanation is required for learning
3. Don't go in depth on business logic
4. Make it very simple and clear
5. Don't add too much content on it
6. The existing design looks good for me
7. Take the content reference from the SPRING_BOOT_LAYERS_GUIDE.md

---

## Reusable Prompt (To recreate this neon cyber design)

Generate a draw.io XML diagram (.drawio file) with the following neon cyber / dark theme design:

### Canvas & Background
- Background: pure black (#000000)
- Shadow enabled on all elements
- No grid

### Node (Box) Style
- Shape: rounded rectangle (rounded=1)
- Fill: none (transparent) — no gradients, no glass effect
- Border (stroke): neon color per layer, strokeWidth=2
- Text: neon color per layer, fontSize=14, fontStyle=0 (not bold), html=1
- Label format: emoji + 2 spaces + Name - Subtitle (single line, no `<br>`)

### Edge (Arrow) Style
- Style: orthogonalEdgeStyle
- Both request and response arrows: dashed=1
- strokeWidth=1.5
- endArrow=block, endFill=1
- flowAnimation=1 (animated flow on all connectors)
- Color: neon color of the SOURCE layer
- Label: fontStyle=0, shadow=1
- Request arrows (top→bottom): center path, exitX=0.5/exitY=1, entryX=0.5/entryY=0, fontSize=12
- Response arrows (bottom→top): right-side routing using fixed mxPoint waypoints, exitX=1/exitY=0.5, entryX=1/entryY=0.5, offset x+=20 per layer to avoid overlap, fontSize=11

### Color Palette (one neon color per layer)
- Layer 1: #00E7FF (cyan)
- Layer 2: #FF007F (pink)
- Layer 3: #1B6EFF (blue)
- Layer 4: #BC13FE (purple)
- Layer 5: #39FF14 (green)
- Layer 6: #F7EB07 (yellow)
- Layer 7: #FF6A00 (orange)

### Legend (left panel)
- Small rounded rectangles, fillColor=none, stroke=layer neon color
- fontStyle=1 (bold), fontSize=11
- List each layer name with its neon color

### Title
- Top center, fontColor=#00E7FF, fontStyle=1, fontSize=22, no border/fill

### Side Labels
- "REQUEST ↓" rotated -90°, left of the flow, fontColor=#00E7FF
- "RESPONSE ↑" rotated +90°, right of the flow, fontColor of last layer

### Layout
- Nodes stacked vertically, centered (~x=380, width=280, spacing ~130px apart)
- Request path: straight down the center
- Response path: offset to the right with staggered waypoints (x=730, 750, 770, 790, 810, 830)

---

### [CUSTOMIZE THIS SECTION FOR YOUR CONTENT]

**Layers (top to bottom):**
1. Client — emoji 🌐, subtitle: Browser / Mobile / Postman
2. [Your Layer 2] — emoji, subtitle
3. [Your Layer 3] — emoji, subtitle
4. [Your Layer 4] — emoji, subtitle
5. [Your Layer 5] — emoji, subtitle
6. [Your Layer 6] — emoji, subtitle
7. [Bottom Layer] — emoji, subtitle (use shape=cylinder3 if it is a Database)

**Request arrow labels (layer N → layer N+1):**
- 1→2: "HTTP Request"
- 2→3: ...
- 3→4: ...
- 4→5: ...
- 5→6: ...
- 6→7: ...

**Response arrow labels (layer N+1 → layer N):**
- 7→6: ...
- 6→5: ...
- 5→4: ...
- 4→3: ...
- 3→2: ...
- 2→1: "HTTP Response"

Output: complete valid .drawio XML file, ready to open in draw.io / diagrams.net