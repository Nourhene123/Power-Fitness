# Legacy stylesheets (source for the design port)

These are the **original CSS files from the PHP `site sport` app**, copied verbatim.
They are the source material for the Angular design port — each one is being lifted
class-for-class into the matching feature/component SCSS during Phase 2.

| File | Ports into |
|---|---|
| `styles.css` | `src/styles/tokens.css` + `base.css` (done) |
| `landing.css` | `features/marketing/home` |
| `about.css`, `AboutUs.css` | `features/marketing/about` |
| `OurProgram.css` | `features/marketing/programs` |
| `bmi.css` | `features/marketing/bmi` |
| `Form.css` | `features/marketing/contact` |
| `LearBegi.css` | `features/marketing/learn` |
| `auth.css` | `features/auth` (login / register split screen) |
| `assessment.css` | `features/assessment` wizard |
| `dashboard.css` | `features/dashboard` + `features/program` + `features/progress` |
| `coach.css` | `features/coach` (layout, plan editor, client progress) |
| `enhance.css` | `shared/directives/ux-enhance` |
| `shop.css` | not ported — shop module was dropped |

Once a stylesheet is fully ported, its file here is deleted. This folder disappears
when Phase 2 completes.
