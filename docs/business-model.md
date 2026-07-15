# Business Model: Other Non-Perennial Crop Growing Operations Coordinator

## Classification

- Repository: `cloud-itonami-isic-0119`
- ISIC Rev. 4: `0119`
- Industry: Growing of other non-perennial crops
- Social impact: food-security, rural-employment, environmental-stewardship

## Customer

- Small-to-medium farms growing fodder/forage crops (fodder corn, alfalfa,
  clover, forage kale)
- Flower and vegetable seed producers
- Cut-flower and ornamental-plant growers (cutting production)
- Smallholder growers of other non-perennial crops not elsewhere classified
  (extension-service integrations)

## Offer

- Field management and record-keeping
- Planting/spraying/irrigation/harvest scheduling coordination
- Crop-health and pest/disease/frost-damage tracking
- Supply procurement coordination
- Audit trail and transparency

## Revenue

- SaaS subscription (per-hectare-per-season pricing)
- Supply chain integration fees
- API access for agronomist/extension-service partners
- Data analytics and reporting add-ons

## Trust Controls

- No direct field-equipment operation without human sign-off
- No finalized pesticide-application decisions by the actor
- All field-operation scheduling proposals are proposals, not commands
- Field registration is required before any operation
- All crop-health concerns are automatically escalated
- High-cost supply orders require approval
- Audit ledger is append-only and never editable

## What we do NOT do

- **Agronomic decisions** (what/when/how much to plant, spray, irrigate,
  harvest) — the farmer/agronomist decides
- **Pesticide-application decisions** — the agronomist/farmer decides
- **Direct field-equipment operation** — the robot manages records and logistics only
- **Economic decisions** (crop mix, marketing, land use) — remain human authority

## Supported Operations

### Field Record Logging
- Planting records (crop, acreage, date)
- Harvest-yield records
- Soil-test data
- Field-condition notes (logging only, not decision-making)

### Field-Operation Scheduling
- Schedule planting, spraying, irrigation, harvest windows
- Track equipment/labor availability
- Propose follow-up field visits (not order them directly)

### Crop-Health Concern Escalation
- Flag suspected pest infestation
- Report disease symptoms or frost damage
- Automatic escalation to farmer/agronomist

### Supply Procurement
- Seed/seedling orders
- Fertilizer orders
- Equipment procurement
- Cost threshold escalation for large orders
