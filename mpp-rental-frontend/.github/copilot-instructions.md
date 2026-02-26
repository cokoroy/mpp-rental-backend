<!-- Copilot / AI agent instructions for mpp-rental-frontend -->
# Project snapshot for AI coding agents

Purpose: give fast, actionable context so an AI can make focused code changes in this frontend.

**Big Picture**
- **Type / Tools:** React (JSX) + TypeScript config present, built with Vite. Dev server: `npm run dev`.
- **Frontend-only SPA:** UI talks to a backend REST API under `http://localhost:8080/api` via a centralized axios instance.
- **Auth flow:** token-based JWT stored in `localStorage` and propagated by `src/services/api.js` interceptors.

**Where to look (quick links)**
- Auth + state: [src/context/AuthContext.jsx](src/context/AuthContext.jsx)
- HTTP client and global error handling: [src/services/api.js](src/services/api.js)
- Auth API helpers: [src/services/authService.js](src/services/authService.js)
- User API helpers: [src/services/userService.js](src/services/userService.js)
- Route protection example: [src/components/ProtectedRoute.jsx](src/components/ProtectedRoute.jsx)
- Login UI: [src/pages/Login.jsx](src/pages/Login.jsx)
- Project config and scripts: [package.json](package.json)

**Critical project behaviors the agent must preserve**
- `api.js` returns `response.data` in the response interceptor — service modules expect that shape (e.g. `{ success, data, ... }`). Do not change that contract without updating callers.
- `api.js` intercepts requests to add `Authorization: Bearer <token>` from `localStorage` and handles `401` by clearing `localStorage` and redirecting to `/login`.
- `authService.login` persists `token` and `user` to `localStorage`. `AuthContext` reads `user` from `authService.getCurrentUser()` on startup.
- Protected routes use `useAuth()` + `isAuthenticated` and `loading` to show spinner or redirect; keep that contract when adding new guarded pages.

**Common patterns and conventions**
- Services live in `src/services/*.js` and return the axios-unwrapped response (`api` already unwraps `.data`). Example: `await api.post('/auth/login', credentials)` in `authService.js`.
- Local storage keys: `token` and `user`. Many parts rely on these exact keys and JSON-serialized `user`.
- UI files use JSX functional components, Tailwind classes, and minimal local form validation (see `Login.jsx`).
- Routing uses `react-router-dom` (v7); use `<Navigate to="/login" replace />` for redirects.

**Developer workflows / commands**
- Start dev server: `npm run dev` (uses Vite). See `package.json` scripts.
- Build: `npm run build` (runs `tsc -b && vite build`). Keep TypeScript build step in mind when adding TS files.
- Lint: `npm run lint`.

**Integration & env notes**
- Backend base URL is hardcoded in `src/services/api.js` as `http://localhost:8080/api`. For environment changes, update that file or introduce environment config (Vite `import.meta.env` preferred).
- Error messages: `api` maps many error responses to string messages. When adding new UI error handling, prefer showing backend `data.message` when available.

**When making changes**
- If altering the API response contract (what `api` returns), update all service callers (`src/services/*.js`) and `AuthContext` accordingly.
- If changing storage keys or token handling, update `authService`, `api` interceptors, and `AuthContext` together.
- Prefer small, focused diffs that update one layer (service, context, or UI) at a time.

If anything in these notes is unclear or you want more examples (e.g., a typical API response shape or routing layout), tell me which area to expand and I will update this file.
