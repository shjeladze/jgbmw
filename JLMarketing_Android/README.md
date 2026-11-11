# JL Programmer Marketing — Android APK
Official & independent BMW / MINI / Rolls-Royce workshop directory, **97 publicly sourced business contacts across 18 countries**.
Android offline app with country/category/brand filters, logo and banner upload, advert preview, CSV import/export, opt-in records, and native Gmail compose with banner attachment.

## Privacy and limitations
- Does not ask for or store Google/Gmail passwords or tokens.
- Uses the user's already-installed Gmail or other mail client. The user reviews and confirms every message before sending.
- Google Sign-In and unattended bulk sending **are not implemented**. They would require a separately configured Google Cloud OAuth client, secure backend and consent-based mailing workflow.
- All 97 bundled source contacts are opted-out by default because publication of a business email is not marketing consent.
- These contacts are an initial list; they are not guaranteed complete, current or deliverable. Check source and legal basis.
- No changes to jlbmw.ge live website or to JLTube.
- Work created in separate branch; default main remains unchanged.

## Build
From JLMarketing_Android:
`gradle --no-daemon :app:assembleDebug`

Output: `app/build/outputs/apk/debug/app-debug.apk`.
Android minSdk 26 / targetSdk 35.
The APK is a debug-signed side-load build and is not Play Store release-signed.
