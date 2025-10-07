Cellenta (iOS) — README

Native iOS client for Cellenta — an online charging / customer management mobile app.
This README covers the ios branch and the Xcode-based iOS app implementation.

⸻

Table of Contents
	1.	Project Overview
	2.	Key Features
	3.	Repository / Branch / Files
	4.	Requirements
	5.	Getting started (run locally)
	6.	Configuration & API
	7.	Project structure & important files
	8.	Architecture & Services
	9.	Testing
	10.	Contributing
	11.	License & Contact

⸻

Project Overview

Cellenta (iOS) is an Xcode project implementing a customer-facing mobile application for an online charging system (purchasing packages, viewing balance, invoices, account management). The iOS project is organized as an Xcode workspace/project and contains UI views, network services, and integration with the backend API.  ￼

⸻

Key Features
	•	Login / Sign-up flow with validation and secure storage of session.
	•	Home dashboard showing user info and balances (minutes, SMS, data).
	•	Store UI for browsing and activating packages.
	•	Bills / Invoices viewing.
	•	Password recovery (send recovery email → verify code → reset password).
	•	Integration with REST API endpoints for authentication, packages, invoices, and password management.

(The sections above match the current implementation goals and existing files in the ios branch.)  ￼

⸻

Repository / Branch / Files

This README targets the ios branch of the Aliduman02/Cellenta repo and the Xcode project contained there (Cellenta IOS.xcodeproj). The branch contains the iOS project, tests, and Info.plist for the app.  ￼

A quick repo snapshot:
	•	Branch: ios (this README targets it).  ￼
	•	Primary Xcode project: Cellenta IOS.xcodeproj.  ￼

⸻

Requirements
	•	macOS with Xcode (recommended: Xcode 14+ / modern Apple Silicon or Intel with latest stable Xcode).
	•	Swift toolchain matching the project (project uses SwiftUI and Swift).
	•	A working backend API accessible from the device or simulator (see Configuration & API).
	•	CocoaPods / Swift Package Manager if the project uses external packages (open the project and follow the dependency dialogs).

⸻

Getting started (run locally)
	1.	Clone the repo (branch ios)

git clone https://github.com/Aliduman02/Cellenta.git
cd Cellenta
git checkout ios


	2.	Open the Xcode project
	•	Double-click Cellenta IOS.xcodeproj or open it from Xcode:

open "Cellenta IOS.xcodeproj"


	3.	Install dependencies
	•	If the project uses SPM, Xcode will resolve packages on open.
	•	If CocoaPods are used, run:

cd ios && pod install
open Cellenta.xcworkspace


	4.	Configure environment / API base URL
	•	See Configuration & API below to set the API base URL and any keys (recommended: use a local Config.plist or a .env file that is excluded from source control).
	5.	Run
	•	Select a simulator or device and press Run in Xcode.

⸻

Configuration & API

Note: The app expects a backend that provides authentication, packages, invoices, and password reset endpoints.

Known API endpoints (used by the project codebase):
	•	POST /api/v1/auth/register — register new users (used in SignUp flow).
	•	POST /api/v1/auth/login — login (used by Login).
	•	POST /api/v1/auth/forgot-password — trigger password reset email.
	•	POST /api/v1/auth/verify-code — verify 6-digit code for password recovery.
	•	POST /api/v1/customers/change-password — change password after verification.
	•	GET  /api/v1/packages — list available packages.
	•	POST /api/v1/customers/invoices — fetch invoices (body may include msisdn).

(If you maintain a different base URL, update the API.baseURL constant or the app’s config file accordingly. Example base URL used during development: http://34.123.86.69.)

Recommended local config approach
	•	Create a Config.plist (or .env) that contains:
	•	API_BASE_URL
	•	APP_ENV (development / production)
	•	Any analytics/3rd-party keys (kept out of repo)

Add Config.plist to .gitignore.

⸻

Project structure & important files

Below are the important files and views to know about when navigating the codebase.
	•	Cellenta IOS.xcodeproj — Xcode project file (open this to run the app).  ￼
	•	HomeView.swift — main dashboard; displays user info and balances and uses a custom tab bar.
	•	StoreView.swift — shows packages; currently fetches packages from /api/v1/packages.
	•	BillsView.swift — invoices screen; should connect to /api/v1/customers/invoices.
	•	ProfileView.swift — profile management and account settings.
	•	AuthService.swift — authentication and password reset related network calls:
	•	login(msisdn:password:)
	•	sendRecoveryEmail(email:)
	•	verifyResetCode(email:code:)
	•	PackageService.swift — package listing and activation helper functions (used by StoreView).
	•	ResetPassword.swift, PasswordForgotCodeEntryView.swift, ForgotPasswordView.swift — views for password recovery flow.
	•	Info.plist — app metadata and entitlements.  ￼

⸻

Architecture & Services
	•	UI Layer: SwiftUI views (Home / Store / Bills / Profile + supporting subviews).
	•	Service Layer: AuthService, PackageService, and network helpers that wrap HTTP calls and JSON decoding.
	•	Persistence: Local session storage uses UserDefaults and/or @AppStorage for msisdn, cust_id, and tokens. (Search for @AppStorage("msisdn") and @AppStorage("cust_id") if you need to change where these persist.)
	•	State management: SwiftUI @State, @StateObject, and @AppStorage patterns.
	•	Networking: Lightweight REST client (URLSession / Codable); adapt or replace with Alamofire if desired.

⸻

Known implementation notes & TODOs
	•	Home and Store currently use mock data in some screens — integrate full responses from the API where necessary.
	•	LoginResponse model fields include: cust_id, msisdn, name, surname, email, sdate — ensure mapping matches backend.
	•	For new signups, the app should still display zero balances (minutes, SMS, data) until the backend populates them.
	•	Invoice endpoint expects msisdn in a POST body — BillsView needs to POST msisdn to /api/v1/customers/invoices.
	•	Password reset verification returns 200 but may require additional handling — if you see unexpected behavior, log full HTTP responses (status and body) for debugging.

⸻

Testing
	•	Unit tests: open Cellenta IOSTests and run tests in Xcode.  ￼
	•	UI tests: open Cellenta IOSUITests to run end-to-end flows in the simulator.  ￼

⸻

Contributing

Contributions are welcome. Suggested workflow:
	1.	Fork the repository.
	2.	Create a feature branch: git checkout -b feat/your-feature.
	3.	Open a pull request describing your changes and the problem solved.
	4.	Include unit tests / UI tests where appropriate.

Please follow the existing code style, keep migrations backward-compatible, and avoid committing secrets.

⸻

Troubleshooting & Tips
	•	If login succeeds but the UI doesn’t navigate, check LoginResponse mapping and where the app writes cust_id/msisdn to UserDefaults vs @AppStorage.
	•	If packages don’t load, verify GET /api/v1/packages response structure and decoding; sample package model fields used in the app:

{
  "package_id": Int,
  "packageName": String,
  "price": Double,
  "amountMinutes": Int,
  "amountData": Int,
  "amountSms": Int,
  "period": Int
}


	•	For password reset verification issues, log the raw HTTP body and headers returned by /api/v1/auth/verify-code to diagnose inconsistencies between mobile client and backend.

