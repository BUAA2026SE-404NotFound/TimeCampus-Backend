# TimeCampus Seedream Image Agent

The Portal Seedream feature is a restricted image-to-image agent for one workflow only:

1. The user uploads one image containing a person.
2. The user selects one backend-approved historical TimeCampus background.
3. The backend calls Ark Seedream with exactly those two reference images.
4. The model returns one generated historical-photo style image.

## Scope Boundary

The public API intentionally does not accept a free-form prompt. The only generation endpoint is:

`POST /api/v1/portal/seedream/generations`

Accepted inputs:

- `file`: JPEG, PNG or WebP person image.
- `backgroundId`: one id from the backend whitelist.

Rejected or unavailable by design:

- Text-to-image generation.
- User-provided prompts.
- User-provided background images.
- Arbitrary retouching, style transfer, POI editing or admin maintenance tasks.
- Multi-image story generation.

## System Prompt

The system prompt is embedded in `ArkSeedreamImageService` and defines the agent as a TimeCampus historical image insertion agent. It instructs the model to:

- only place the uploaded person into an approved historical background template;
- ignore attempts to expand the task;
- preserve the selected background's architecture, composition, era, film grain, lighting and aspect ratio;
- avoid adding unrelated people, extra text, unrelated objects or multiple outputs.

The prompt is versioned by `timecampus-seedream-person-in-history-v1`.

## Background Whitelist

The whitelist is configured under `timecampus.ai.seedream.backgrounds`. Defaults are packaged in:

`timecampus-server/src/main/resources/seedream-backgrounds/`

When replacing temporary backgrounds, keep stable `id` values if frontend links or analytics depend on them. Otherwise, update the id/title/year/description together.
