---
name: vfx-model-texture-animation-all-in-one
description: All-in-one instructions for Minecraft/Blockbench-style VFX model geometry, strict UV-template texture work, animation analysis and rebuilding, and validation.
---

# VFX Model + Texture + Animation Rebuild Skill

## Purpose

Use this workflow whenever a user supplies any combination of:
- a model / geometry JSON file;
- a texture-template image that shows UV islands and intentionally empty areas;
- an animation JSON file;
- a VFX concept, element, ability description, or art direction.

The deliverable is a coherent asset set whose **geometry, texture atlas, UV coordinates, and animations agree with one another**. Do not merely generate a pretty texture or a concept image and claim the model is complete.

Typical visual target: polished, modern stylized voxel game with crisp retro pixel-art textures, deliberate silhouettes, readable layered VFX shapes, strong value contrast, and a disciplined palette.

## Non-negotiable rules

1. **Inspect the actual files first.** Parse the complete JSON files and inspect image dimensions, mode, alpha channel, UV regions, bone names, and existing animation clips. Do not infer unseen details from a truncated snippet.
2. **The template is a UV map, not a reference texture to copy.** Treat its colored/filled regions as the places that must receive artwork. Preserve intentionally empty blank/background areas as transparent. Never paint into empty space just to make the atlas look full.
3. **Match the template's exact dimensions.** The texture output must have the same width and height as the supplied template unless the user explicitly requests a new resolution. If geometry declares different dimensions, resolve the mismatch intentionally and update geometry UVs and texture dimensions together.
4. **Every exported UV must match a valid occupied template region.** It is not enough for UV rectangles to be within image bounds. Check that each face's UV rectangle is placed in the intended filled region and does not cover blank space. For a rebuilt geometry, re-pack/reassign UVs to the template regions or explain that the supplied template is incompatible and ask before replacing the map.
5. **Don't claim tests that weren't run.** Distinguish JSON/mask/UV checks from an actual Blockbench, game-engine, or in-game render. Label generated preview images honestly as approximate geometry previews or concept illustrations.
6. **Do not claim gameplay behavior comes from the model.** Geometry, textures, and animation do not themselves apply damage, knockback, buffs, debuffs, fire resistance, or hit detection. State that runtime code/VFX integration is needed for gameplay effects.
7. **Preserve useful source behavior.** If an animation file is supplied, analyze its clips, timing, loops, keyframes, and bone names. Retain or deliberately migrate useful behavior; never silently drop clips or animate nonexistent bones.
8. **Deliver real files.** Save actual exported assets and verify their existence before linking them. Provide direct links to the individual geometry, texture, animation (if relevant), previews, and validation/README files. Do not create a ZIP archive unless the user explicitly asks for one.
9. **Do not simply add cubes to inflate complexity.** Improve the silhouette and motion readability. Each new cube, bone, or layer should have a visual/animation purpose.
10. **Use the source's target format.** Geometry JSON with `format_version: 1.12.0` and animation JSON with `format_version: 1.8.0` are Bedrock-style formats. Do not assume they are directly usable by every Minecraft Java renderer or GeckoLib version. Preserve the requested format and clearly note compatibility limits when relevant.

## Inputs and task modes

Determine which task the user is asking for:

- **Texture-only:** preserve the existing geometry and UVs; draw into the template's occupied mask.
- **Upgrade model + texture:** refine silhouette and/or add meaningful layers, then make the atlas and UVs agree. Prefer preserving bone names when possible so animation compatibility survives.
- **Full rebuild:** make a coherent new model, UV layout, texture, and animation from the concept. Keep the provided template as a hard constraint unless the user authorizes a new map.
- **Animation upgrade:** inspect and enhance supplied timing and motion, maintaining clip names or documenting renames.
- **Pack/integration request:** provide the required individual files, previews, README, and compatibility notes. Do not archive them unless explicitly requested.

If a critical ambiguity prevents the work (for example, no clear way to distinguish unused black space from a legitimate black texture island), ask one short question. Otherwise use grounded defaults and proceed.

## Phase 1 — inventory and inspect

### 1. Inspect the template image

Record:
- exact pixel width and height;
- image mode (`RGB`, `RGBA`, etc.);
- alpha-channel distribution and whether it is meaningful;
- likely background / unused-space color (often black in these UV maps);
- filled UV islands, thin borders, and narrow strips;
- whether the image is a strict template whose mask must be preserved.

**Mask interpretation procedure:**

1. If transparent pixels already encode empty regions, treat alpha as the main occupancy signal and preserve the alpha mask exactly where possible.
2. If the template is opaque and clearly uses solid black to mark unused areas, treat those black regions as empty only after visually confirming that convention. Filled placeholder islands may include pale blue, gray, green, pink, yellow, or other flat guide colors; those colors are not the target art palette.
3. Preserve all valid occupied islands—including dark-gray placeholder islands. Do not confuse dark-gray filled regions with black empty space.
4. If background semantics are ambiguous, inspect connected regions and colors or ask the user rather than guessing.
5. Use nearest-neighbor / hard pixel edges for pixel-art masks. Avoid antialiased mask resizing.

Create a binary or alpha occupancy mask `M` from the template's actual convention. If the original uses a full alpha mask, preserve actual alpha values as well as occupied pixels where appropriate. Save a copy of the original template as a reference artifact; do not overwrite the user's source.

### 2. Inspect geometry JSON

Parse the complete file. Record:
- `format_version`, geometry identifier, `texture_width`, `texture_height`;
- all bones and parent relationships;
- every cube's `origin`, `size`, `pivot`, `rotation`, and per-face UV definition;
- which faces use explicit UV mappings and which are omitted;
- UV rectangle dimensions and coordinates, including negative/fractional values;
- existing or duplicated UV islands, intended sharing, gaps, and out-of-bounds mappings.

Do not assume a UV mapping is correct merely because it parses. Cube dimensions and UV dimensions can vary by exporter, so compare with the source's explicit layout and template before rewriting them.

### 3. Inspect animation JSON when supplied

Record:
- `format_version`, all animation names, `loop`, and `animation_length`;
- every animated bone and whether that bone exists in geometry;
- position, rotation, scale, key times, interpolation-style assumptions, and Molang expressions;
- the clip's intended phases, movement direction, speed, and existing timing.

Flag keyframes later than `animation_length`, missing bone names, abrupt jumps, clips with no useful motion, and conflicting animations that should not be played at once. Keep a brief source-analysis note in the README when it helps explain the upgrade.

## Phase 2 — plan the effect before editing

Write a compact design plan before constructing the asset:

- **Silhouette:** what reads clearly at game distance? Examples: crescent slash, radial ward, broken ring, erupting spires, central implosion, lightning dash, solar nova.
- **Primary motion:** where does the effect begin, travel, expand, or collapse?
- **Secondary motion:** arcs, tendrils, fragments, embers, orbiting shards, heat ripples, afterimages.
- **Visual hierarchy:** primary focal shape > inner bright layer > outer energy shell > small accents.
- **Palette:** usually 4–8 coherent base shades plus highlight colors. For high-energy effects use a bright, saturated highlight, medium body tone, darker edge, and limited accents.
- **Animation phases:** anticipation/charge, ignition/release, expansion or travel, lingering state, and cleanup/collapse as appropriate.
- **Gameplay boundary:** list what is visual only and what requires runtime code.

Do not create visual clutter for its own sake. A few strong layers are better than many tiny cubes that merge into noise.

## Phase 3 — geometry and bone strategy

### Geometry conventions

- Preserve the source's geometry identifier unless a new identifier is necessary.
- Use a real root bone and unique, descriptive bone names (`core`, `main_arc`, `inner_flame`, `shockwave`, `afterimage`, etc.).
- Keep parent names valid and avoid cycles.
- Use the geometry representation actually supported by the target format. In Bedrock cube geometry, cubes are axis-aligned boxes modified by pivots and rotations; they are not arbitrary polygon meshes.
- Approximate smooth arcs, tapered streaks, and organic flames with progressively offset/rotated overlapping segments. Keep the silhouette legible.
- Avoid enormous overlapping planes that hide the central effect or create a blocky rectangle where a ring/crescent is expected.
- Check scale, visible bounds, pivots, forward direction, and whether the effect is intended to sit on the ground or move through the air.
- If animation compatibility matters, prefer preserving existing animated bone names. If a bone must be renamed or split, update all references in the animation JSON.

### UV strategy for changed geometry

There are two allowed routes:

**A. Preserve the existing template islands.** Prefer this when the user says the template is mandatory. Assign each revised face to a matching existing island or compatible region. The new geometry may be more detailed, but its faces must be mapped to actual occupied template regions.

**B. Rebuild the UV layout within the template's occupied footprint.** Use this only when the existing island layout cannot reasonably support the revised geometry. Keep the same image dimensions and keep the unoccupied area empty. If you materially change the map, export a new labeled UV reference and explain the change; do not claim the original island layout was preserved.

Never scale the template just to hide a mapping problem. If the model's declared texture size differs from the image, choose and document a consistent UV coordinate system, then update all related coordinates and dimensions together.

### UV checks

For every explicitly mapped face with UV origin `(u, v)` and size `(du, dv)`, check:

- `u >= 0`, `v >= 0`, `du > 0`, `dv > 0`;
- `u + du <= texture_width`, `v + dv <= texture_height`;
- the corresponding template pixels are occupied (not blank/transparent), except for intentional UVs whose format semantics require a documented exception;
- the assigned region is large enough and oriented appropriately for that face;
- any overlap with another UV is intentional (e.g. purposeful texture reuse/mirroring) rather than accidental;
- all visible faces have UV data or are handled according to the target format's documented default behavior.

Do not report an overlap count or UV count until it has actually been computed from the final exported JSON. State exceptions honestly.

## Phase 4 — texture construction

### Target style

Use a modern stylized voxel / retro pixel-art look:
- hard pixel edges; no soft painterly blur;
- readable clusters rather than random noise;
- intentional light direction, contrast, edge highlights, cracks, streaks, or facets;
- enough distinct value steps to read at small sizes;
- a transparent background wherever the template indicates no island.

### Mask-safe texture workflow

1. Generate or draw the artwork at the template's exact resolution. Artwork may be produced procedurally or from an image-generation result, but the final deliverable must be mask-clipped and validated.
2. Work on an RGBA image. Do not resize the mask with bilinear/bicubic filters.
3. Create `raw_art` independently of the occupancy mask.
4. Apply the mask at the end: outside the mask, output alpha must be 0 (unless the template explicitly uses a different alpha convention); inside, fill the islands completely.
5. Where the template has explicit partial alpha that is intentional, preserve or deliberately combine that alpha rather than flattening it.
6. If using an image-generation tool, don't trust its atlas layout or resolution by itself. Crop/scale artwork as needed, then apply the exact template mask with code.
7. Preserve narrow UV strips. Make them visually useful (thin edge colors, ember lines, bevel/shade runs) without spilling into blank neighboring areas.
8. Avoid drawing labels, grids, previews, checkerboards, or text into the actual texture unless the user explicitly requests them.

### Template mask validation

At final export, compare the intended occupancy mask `M` to the exported texture's alpha mask `A`:

- for binary alpha templates, compare `M == (A > 0)` pixel-for-pixel;
- for templates with meaningful graded alpha, compare actual alpha where required;
- if the opaque black background was interpreted as blank, verify every such blank pixel is transparent in the output;
- verify every filled placeholder island has texture pixels and none remains the original guide color due to an incomplete fill;
- create an atlas preview with the original mask/template shown beside or overlaid only as a **preview artifact**, never baked into the runtime texture.

If the mask does not match, fix it before delivery. Do not claim a pixel-identical match unless a script computed it.

## Phase 5 — animation analysis, design, and enhancement

Animation is part of the asset, not an optional flourish. If an animation file is supplied, inspect and improve the actual animation JSON, not just describe a motion concept. If no animation is supplied but the task requests an animated effect, create a compatible animation file after checking the target runtime's supported format.

### 5.1 Analyze the source animation before changing it

Create an inventory of every clip and track. For each clip record:
- exact clip name, loop setting, animation duration, and intended use (one-shot, persistent loop, transition, or outro);
- all animated bone names and channels (`position`, `rotation`, `scale`, plus any supported properties);
- keyframe times and values, expressions/Molang formulas, interpolation modes if explicit, and the first/last pose;
- which motion already works and should be preserved;
- defects: missing bones, invalid or out-of-range key times, unsorted timing, sudden transform snaps, pointless frozen spans, bad loop seams, or expressions unsupported by the target renderer.

Compare the animation against the geometry's exact bone names and pivots. A clip referencing a bone that was removed, split, or renamed is not compatible until its tracks are migrated. Do not quietly discard existing clips. Preserve clip names where practical; if a rename is necessary, document the old and new names in the README.

### 5.2 Decide the animation contract

Before adding keyframes, explicitly define:
- **trigger / use:** spawn, attack, movement trail, hit confirm, persistent ward, charge-up, detonation, or cleanup;
- **duration:** how long the clip really needs to run and why;
- **loop behavior:** one-shot, seamless loop, or hold-last-frame (only when supported by the target format);
- **start pose:** transforms at time zero, including whether the effect begins hidden, small, offset, or already visible;
- **end pose:** transforms at the final timestamp and what the runtime should do next;
- **playback relationship:** which clips may play simultaneously, which must not overlap, and which bone tracks they share;
- **renderer features:** whether transparency animation, material changes, emissive rendering, particles, billboards, or runtime-driven variables are actually supported.

Never make an effect one continuous animation if the user needs independently timed phases, but also do not split it into many clips unless the runtime can orchestrate those clips coherently.

### 5.3 Build an intentional phase timeline

Match the phase plan to the effect rather than applying one generic animation to everything. For cinematic one-shot effects, a useful pattern is:

1. **Anticipation / charge:** low scale or restrained motion; core pulses or rotation accelerates. Keep it brief unless the user asks for a long charge.
2. **Release / ignition:** a sharp, readable burst. Use fast expansion, a snap, or a direction change rather than a long slow swell alone.
3. **Main action:** move the main silhouette clearly—slash sweeps, dash travels, spires erupt, a ring expands, a ward walls up, or void folds close inward.
4. **Secondary detail:** stagger tendrils, shards, rays, fragments, or embers slightly behind the main action so the effect has depth and a readable focal point.
5. **Lingering / impact:** preserve a readable silhouette briefly; let secondary motion settle or trail behind.
6. **Collapse / cleanup:** retract, shrink, fade using supported features, or move off-screen. End cleanly without a visible jump or permanent stray geometry.

For short attack VFX, compress phases into a fraction of a second. For a cinematic multi-phase nova, spread them over several seconds only when each section has real movement. Length is not a quality metric by itself.

### 5.4 Animate for readability and physical intent

- Animate the primary visual first. Make its beginning, direction, expansion, impact, and ending understandable at ordinary playback speed.
- Use secondary motion with staggered timing (small offsets between bones) rather than moving every piece in lockstep.
- Vary scale and rotation deliberately; avoid random-looking wobble without a visual purpose.
- For traveling effects, animate root or a parent controller for the trajectory, and animate local details on child bones. Keep local and world-space intent clear.
- For a radial effect, scale ring radius in a way the actual geometry supports. A single cube scaled uniformly may become a square plate, not a circular ring; use ring segments or a supported mesh/particle system if a convincing ring is required.
- For a slash arc, animate sweep/rotation and trail timing—not only translation on a straight axis.
- For eruption, bring spires upward in staggered bursts, allow a brief hold, then retract or fade. Avoid having all spires appear on the same frame unless the design calls for a single explosive snap.
- For an implosion, animate fragments or sheets inward toward a defined center before collapse; do not confuse expanding and imploding motion.
- For orbiting fragments, use loop-safe rotational motion and stagger phase where the target format/runtime supports it.
- Respect pivot positions. A rotation around the wrong pivot can turn a flame arc, shard, or ring into a propeller-like motion.
- Keep transform magnitudes plausible for the model's scale. Huge scale values and multi-thousand-degree rotations may be technically legal in some contexts but can look unstable and are difficult to tune.
- Do not animate non-existent material properties. Geometry scale does not automatically implement opacity, emissive intensity, particle trails, camera shake, sound, damage, knockback, or gameplay status effects.

### 5.5 Keyframes, easing, and timing quality

- Put important keys at phase boundaries, then add intermediate keys only when they improve the trajectory.
- Make key timestamps ascending and within `[0, animation_length]`.
- Specify start and end states intentionally for all important tracks. Missing tracks inherit the runtime's behavior; do not assume they reset automatically.
- Use supported interpolation/easing features when the target format exposes them. If only simple key values are supported, approximate acceleration and deceleration with well-spaced intermediate keys.
- Use fast acceleration for the initial strike, controlled deceleration for a lingering ring, and an abrupt release for a detonation where appropriate.
- Avoid identical values repeated across many keys unless they mark an intentional hold.
- Avoid discontinuities where a track's final state does not match the next clip's start state.
- Test loop seams: the last pose should flow into the first pose, including rotation, position, scale, and all moving child bones. A loop that visibly snaps is not seamless.
- Keep idle loops subtle and stable. Do not loop one-shot slash, detonation, eruption, or collapse animations unless a continuous loop was explicitly requested.
- Check expression units and clock variables. Do not blindly copy a sine expression from another file; ensure the period and angular units produce the intended speed in the target format. Use the original file's conventions where supported, and document assumptions.
- Use explicit keyframe values for critical impacts when deterministic timing matters. Use expressions only when they're supported and genuinely useful for repeated motion.

### 5.6 Geometry-animation compatibility after edits

After a geometry change:
1. Collect the final geometry bone names and parent hierarchy.
2. Collect every bone referenced by every animation clip.
3. Detect missing or renamed bones and decide whether to preserve the old name, migrate its keys, or intentionally remove that track.
4. Update tracks for new parts: any added animated spire, shard, afterimage, ray, ring, flame tongue, or ember cluster needs a corresponding bone only if independent motion is required.
5. Avoid a single parent animation fighting child animations. Check duplicate position/rotation/scale tracks across clips that may run simultaneously.
6. Verify pivots and origin-relative motion by inspecting the resulting model, not only by reading the JSON.
7. If the asset uses a geometry format whose animation conventions differ from the supplied animation format, do not silently mix formats. Explain the migration required and provide output in the requested target format when feasible.

If the model is being changed but animation must stay compatible, preserving existing bone names and pivots is usually safer than gratuitously renaming everything. If a full rebuild is requested, redesign the hierarchy with clear controller bones (for example `root`, `core`, `main_arc`, `trail`, `shockwave`, `fragments`) and rewrite the animation tracks against that new hierarchy.

### 5.7 Animation validation checklist

Automate these checks where possible:
- animation JSON parses successfully;
- all clips have a valid `animation_length` and intentional `loop` value;
- key times are numeric, ordered, and do not exceed duration;
- every bone track points to a bone in the final geometry;
- every transform value has the correct shape and valid numeric values, or a documented supported expression;
- no accidental duplicate clip names or missing required clips;
- loop clips have compatible start/end poses (automated approximation plus visual inspection);
- one-shot clips finish in a clean pose and do not leave geometry visible by accident;
- any animation properties beyond transform channels are supported by the target renderer;
- the animation preview was checked at phase boundaries and at normal playback speed, if a renderer/tool is available.

JSON parsing and reference checks do not prove that motion looks good. Label validation accurately: static checks, generated playback preview, Blockbench preview, and in-game test are different levels of evidence.

### 5.8 Suggested animation deliverables

When animation was supplied or requested, include:
- the revised animation JSON;
- a clip summary table in the README (`name`, duration, loop/one-shot, phase purpose, notable tracks);
- a phase timeline (text or image) when it helps review cinematic timing;
- animation validation results and known limitations;
- a note about what still needs runtime orchestration or gameplay code.

Geometry, texture, and animation are visuals only. Fire Resistance, damage, ignition, knockback, enemy repulsion, collision/hit detection, and other gameplay outcomes must be implemented in separate runtime code/procedures.

## Phase 6 — previews and honest validation

Create helpful previews when tools permit:
- the actual texture atlas at native resolution;
- a geometry silhouette preview made from the exported JSON;
- optionally a mockup/composite showing the VFX shape and palette.

Label them accurately:
- **JSON geometry preview / approximation** if rendered by a custom script;
- **concept illustration** if image-generated;
- **Blockbench screenshot** only if captured from Blockbench;
- **in-game render** only if produced in the actual target game / renderer.

Do not use a pretty concept illustration as proof that the exported model renders correctly.

## Phase 7 — automated checks

Perform as many of these as the tools allow, and report only checks actually run:

### File / JSON checks
- All JSON parses successfully.
- Geometry identifier exists and texture dimensions match the exported PNG.
- Bone names are unique and parent references are valid.
- Every cube has valid numeric `origin` and positive `size` values.
- Every UV lies within texture bounds.
- Every animation clip has a valid duration and valid key data.
- Every animated bone exists in geometry.

### Texture checks
- Exported PNG dimensions equal the template dimensions exactly.
- Mode is RGBA if transparency is needed.
- The final empty-space mask matches the template's interpreted mask pixel-for-pixel.
- All UV rectangles are over occupied template pixels and inside image bounds.
- UV overlap is absent unless intentional and documented.
- No guide colors, temporary borders, or preview labels were accidentally left in texture islands.

### Export checks
- Every expected individual file exists and opens.
- The geometry, texture, animation, and previews correspond to the same final asset version.
- Direct download paths exist before linking them.

Suggested Python libraries: `json`, `Pillow` (`PIL`), and optionally a geometry-preview library/script. Keep validation scripts in the work folder when useful; export them separately only if they help reproducibility or future edits.

## Deliverables

For a full model + texture + animation upgrade, normally export:

- `vfx_<name>.geo.json` — final geometry;
- `vfx_<name>_texture.png` — exact-resolution RGBA texture;
- `vfx_<name>.animation.json` — revised animation, if supplied/requested;
- `vfx_<name>_atlas_preview.png` — texture atlas preview;
- `vfx_<name>_geometry_preview.png` — approximation or real-render preview, correctly labeled;
- `template_reference.png` — original template copied without modification;
- `README.md` — file list, dimensions, clip description, target format assumptions, validation actually run, known limitations;
- `vfx_<name>_validation.txt` — checks actually run and known limitations.

Use clear, stable file names and avoid ambiguous `_final_final2` variants.

## Recommended README sections

1. Asset concept and intended silhouette.
2. File list and exact texture dimensions.
3. Geometry changes (bones/cubes and why).
4. Animation clips, duration, phases, and loop status.
5. UV/template method and mask-validation result.
6. Renderer/format assumptions and compatibility caveats.
7. What is visual-only versus what requires gameplay code.
8. Checks run and checks not run (especially no in-game test if none occurred).

## Response format to the user

Keep the response compact but concrete:
- one sentence naming the VFX interpretation;
- direct links to each individual deliverable;
- short summary of model/texture/animation changes;
- validation table with real counts/results, not invented claims;
- one clear note on untested render/game compatibility.

Never output fake links, say an export is complete before the files exist, or state “perfect” without objective validation. If a requested task couldn't be completed, say exactly which part is missing and provide only verified artifacts.

---

# Appendix A — Detailed Animation Reference

This appendix provides an extended animation checklist. Follow the main skill's phase 5 as the primary workflow; use this section as a supplemental audit reference.

# Animation Guide for Minecraft VFX Models

Use this guide alongside `SKILL.md`. The goal is to upgrade actual animation files that work with the supplied model and target renderer—not to write a motion description while leaving the JSON unchanged.

## A. First, audit the source

For every clip, record:

| Item | Questions to answer |
|---|---|
| Name and purpose | What triggers this clip? Is it an attack, idle, loop, transition, or cleanup? |
| Duration | Does the timing suit the effect? Is the clip extended by useful motion or just empty time? |
| Loop | Is it one-shot, looping, or holding a final pose? Is that behavior supported by the format? |
| Tracks | Which bones have position, rotation, scale, or other channels? |
| Keyframes | Are times ordered and inside the clip duration? Are the values and expressions valid? |
| Start/end state | Does the clip begin and end in intentional poses? Will another clip transition cleanly? |
| Bone compatibility | Does every animated bone exist in the final geometry with the expected pivot? |
| Renderer compatibility | Are expressions, interpolation modes, alpha/material changes, and other features supported? |

Read the complete files rather than trusting partial previews or excerpts. Do not discard useful source motion without documenting why.

## B. Design a motion timeline before keyframing

Use only the phases suited to the effect:

1. **Anticipation / charge** — core forms, energy gathers, restrained pulse or rotation builds.
2. **Release / ignition** — fast expansion, snap, directional launch, or sudden reveal.
3. **Primary action** — slash sweeps, dash travels, spires erupt, ring expands, sheets implode, or shield segments rise.
4. **Secondary motion** — fragments, tendrils, embers, and trails follow with deliberate timing offsets.
5. **Impact / linger** — preserve a readable shape long enough for the effect to register.
6. **Collapse / cleanup** — retract, shrink, fade using supported rendering features, or move away; end without stray geometry.

Not every effect needs all phases. Short slashes may take 0.3–0.9 seconds; a cinematic detonation may last several seconds. Treat those only as rough design ranges, not technical requirements. Timing should follow the gameplay and art direction.

A longer animation must contain meaningful motion. Avoid a slow, frozen hold added solely to increase duration. If a sustained ward needs to remain active, separate its one-shot ignition, looping ward, and collapse clips when the runtime can orchestrate them.

## C. Match tracks to hierarchy

- Use a parent/controller bone for global travel, orientation, or overall scale when appropriate.
- Use child bones for local arcs, sparks, shards, flame tongues, spires, and afterimages that need independent movement.
- Check pivots: the correct rotation around the wrong pivot still produces incorrect movement.
- If geometry is edited, diff the old and new bone names. Preserve animated names and pivots when practical; otherwise migrate their tracks deliberately.
- Ensure every animated bone exists in the final geometry. Never leave stale tracks after renaming or deleting bones.
- Avoid simultaneous clips fighting over the same position/rotation/scale channel unless layering/blending is explicitly supported and intended.
- Do not assume root translation, local bone translation, and world-space trajectory are interchangeable. Confirm the target runtime's transform conventions.

## D. Keyframe quality

- Place keyframes at important phase boundaries.
- Use additional intermediate keys to form a clear arc, acceleration, recoil, settling, or retraction—not random motion.
- Use supported easing/interpolation features where available. Otherwise, approximate timing with intermediate keys.
- Verify times are ordered and between zero and `animation_length`.
- Give all important tracks intentional initial and final states.
- For loops, compare the last and first poses across every moving bone and channel. A loop seam must not snap.
- Prefer restrained idle motion; reserve extreme movement for release or impact.
- Avoid unnecessarily huge scale multipliers or extreme rotation values without a clear visual reason.
- Inspect expressions such as `query.anim_time` and `math.sin(...)` against the actual clip length and engine conventions. Do not paste formulas blindly from another file.
- Do not use scale-to-zero as a fake transparency fade if it creates artifacts or doesn't match the intended renderer.

## E. Format and runtime guardrails

- Preserve the source `format_version` and syntax unless the user specifies a target format or migration.
- Bedrock geometry/animation JSON and Minecraft Java animation systems are not interchangeable by default. Check the intended loader, renderer, and version.
- Only use loop values, easing syntax, Molang expressions, material channels, alpha animation, and other features supported by that target.
- The geometry animation does not create particles, sound, camera shake, light emission, hitboxes, damage, knockback, ignition, or status effects by itself. Those require separate renderer features or gameplay/runtime code.
- When no compatible preview tool is available, run structural checks and clearly state that playback was not visually tested.

## F. Validation checklist

- [ ] Animation JSON parses.
- [ ] Every clip has a positive, intentional duration and loop behavior.
- [ ] All key times are numeric, ordered, and within clip duration.
- [ ] Every animated bone exists in final geometry.
- [ ] Bone parent relationships and pivots are valid.
- [ ] Transform arrays have the expected number of numeric values or valid target-supported expressions.
- [ ] Loop clips have a compatible start/end pose; visually inspect where possible.
- [ ] One-shot effects end cleanly and do not leave visible geometry unexpectedly.
- [ ] Clip names, transitions, and concurrent tracks are documented.
- [ ] Preview method is identified honestly: structural-only, scripted approximation, Blockbench playback, or in-game render.

## G. README animation table

Include a table like this in each VFX asset pack:

| Clip | Duration | Loop? | Purpose / phases | Animated bones |
|---|---:|---|---|---|
| `ignite` | 1.0 s | No | Charge → release | core, outer_arc |
| `active` | 2.0 s | Yes | Persistent pulse / orbit | core, fragments |
| `collapse` | 0.7 s | No | Retraction / cleanup | root, outer_arc, fragments |

Replace these illustrative values with the actual exported clip names, durations, and bone tracks. Never report guessed values as validation results.

---

# Appendix B — Copy-and-Paste Project Prompt

Use this prompt for each new VFX request. Attach the geometry JSON, UV template, and animation JSON when available.

# Reusable Prompt — Rebuild a VFX Model, Texture, and Animation

You are my technical VFX asset artist and geometry/UV/animation engineer. Work directly from the files I attach. Do not just give advice or a concept image: produce the actual updated asset files.

## My requested VFX
**Ability / effect:** `[INSERT NAME AND DESCRIPTION]`

**Desired look:** modern stylized voxel game with crisp retro pixel-art textures, strong readable silhouette, coherent value contrast, and polished VFX layering.

## Attached source files
- Geometry JSON: `[ATTACHED MODEL JSON]`
- Texture template: `[ATTACHED TEMPLATE IMAGE]`
- Animation JSON, if available: `[ATTACHED ANIMATION JSON]`

## Critical template rule
My texture image is a strict UV template. Its colored flat regions show where texture pixels must be filled; its blank/black/transparent regions show where texture must remain empty. Inspect the actual image and alpha channel to determine the exact mask instead of guessing. The final PNG must have exactly the same pixel dimensions as the template. Fill every intended UV island with artwork; keep every unused region empty/transparent. Do not put decoration into the blank areas, and do not resize the template.

## Animation is a first-class deliverable
If an animation file is attached, you must inspect and improve the real file—not merely recommend animation ideas. Keep useful clips and preserve names when possible. Reconcile the animation tracks with the final geometry after every bone/hierarchy edit. Define each clip's duration, loop/one-shot behavior, phases, start/end state, and transition purpose. Use meaningful motion throughout long clips, stagger secondary elements, and ensure looping clips return to a compatible first pose. Do not animate nonexistent bones or rely on unsupported shader/material/alpha features. If the renderer cannot preview animation, say so and report static checks separately from visual verification.

## What to do
1. Inspect the complete geometry, template, and animation files before editing. Report the true dimensions, model bones/cubes, current UV layout, animation clips, duration, and problems found.
2. Identify whether this is texture-only, an upgrade, or a full rebuild. Improve the silhouette and visual hierarchy according to the effect description. Avoid adding cubes without a clear purpose.
3. Rebuild or update UVs deliberately. Keep every face mapped to an occupied template region and within image bounds. If new geometry cannot use the old islands correctly, re-pack UVs within the template's occupied area and update geometry UVs to match. Never claim compatibility without checking the actual UV rectangles against the mask.
4. Create a detailed, pixel-art texture with a limited, intentional palette. Use crisp pixel clusters, highlights, shadows, and readable effects. Use generated artwork only as a source; clip the final texture to the exact template mask with code.
5. If an animation is supplied, analyze every clip before editing: clip name, duration, loop behavior, animated bones/channels, keyframe times, start/end poses, expressions, and likely defects. Preserve useful motion and clip names where practical; document intentional renames.
6. Design a real timeline appropriate to this effect. Use anticipation/charge, release, main motion, staggered secondary motion, impact/linger, and collapse/cleanup only where useful. Do not make clips longer by leaving the model frozen. Specify what plays once, what loops, and how clips transition or overlap.
7. After geometry edits, reconcile every animation bone and pivot with the final geometry. No missing bones, out-of-range keyframes, unintended track conflicts, broken loop seams, or unexplained start/end jumps. Use only interpolation, expressions, alpha/material animation, and other features supported by the intended renderer.
8. Keep the source format unless there is an explicit target-format reason to convert. Warn me if this JSON format is not directly compatible with my intended Minecraft renderer.
9. Export the real geometry JSON, matching RGBA texture PNG, revised animation JSON (when applicable), atlas preview, honest geometry/animation preview if available, README, and validation report as separate files. Do not create a ZIP archive unless explicitly requested.
10. Run automated checks: parse JSON; check texture/model dimensions; validate bone names and parents; check all UV bounds and map each UV region to the actual occupied template pixels; compare the final empty-space mask against the template pixel-for-pixel; check animation-bone references, key times, loop settings, and transform data.
11. Report only checks you actually ran. Do not call a concept illustration an in-game render. Do not claim the model/texture/animation applies buffs, damage, ignition, knockback, or hit detection; those are runtime gameplay-code features.

## Must deliver
- Geometry JSON
- Texture PNG at exact template resolution
- Revised animation JSON, if an animation file was provided or requested
- Atlas preview and geometry preview
- Individual geometry, texture, animation (if applicable), previews, README, and validation report files
- Validation summary with real counts and known limitations

If something is ambiguous but not blocking, make a reasonable assumption and state it. If the empty-space convention cannot be safely identified, ask one focused question before editing. Be meticulous: visual quality matters, but correct UV-to-texture mapping matters just as much.
