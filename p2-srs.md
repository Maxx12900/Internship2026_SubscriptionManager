Team: Placeholder, Group: FAF-251
SNS (Software requirements specification)
Subscription Manager (Android application)
1. Introduction
1.1 Purpose. This document specifies the requirements of Subscription Manager, an Android application that helps users track recurring subscriptions, see their costs and renewal dates, and receive reminders before renewal.
1.2 Scope. In scope: local subscription tracking, reminders, budget indicator, settings. Out of scope in this version: payment processing, cloud synchronization, storage of card numbers.
1.3 Definitions. Subscription: a recurring paid service tracked by the user. Billing period: the interval between payments. Renewal date: the date of the next payment. Package name: the Android identifier of an installed application. Bank statement: a CSV or PDF export from the user’s bank.
1.4 References. Jira backlog (epics, user stories, acceptance criteria); UML diagrams (use case, activity, sequence); Figma wireframes and mockups.
2. Overall description
2.1 Product perspective. A standalone Android application. All data is stored locally in a Room (SQLite) database. There is no server.
2.2 User classes. One class: the device owner, an individual who manages personal subscriptions.
2.3 Operating environment. Android 8.0 (API 26) and above. Kotlin, Jetpack Compose, Room.
2.4 Constraints. No card numbers or CVV codes are stored. Installed-app detection depends on Android package visibility. 
2.5 Assumptions. The user grants the notification and usage-access permissions for the related features.
3. Functional requirements
3.1 Subscription list (FR-LIST)
FR-LIST-01: The system shall display all stored subscriptions as a list on the home screen, showing for each one the icon, name, price and Active/Inactive status. 
FR-LIST-02: The system shall show the icon of the installed application associated with a subscription and, if no application is associated or it is not installed, a letter avatar made from the first letter of the name. 
FR-LIST-03: The system shall open the details screen when the user taps a subscription in the list.
FR-LIST-04: The system shall allow the user to sort the list by name, price or next renewal date and to filter it by category. 

3.2 Adding a subscription (FR-ADD)
FR-ADD-01: The system shall provide an Add Subscription screen with the fields: App, Subscription name (required), Category (required), Price (required), Billing period (required), Start date (required), Days before to remind (optional), Description (optional). 
FR-ADD-02: The system shall not save a subscription and shall show an error message when the name is empty or the price is not a valid number. 
FR-ADD-03: The system shall accept only digits and one decimal point in the Price field, and only digits in the Days before to remind field.
FR-ADD-04: The system shall format the price to two decimal places when the Price field loses focus.
FR-ADD-05: The system shall let the user choose the start date with a date picker dialog.
FR-ADD-06: The system shall let the user choose the billing period from a fixed list and the category from a fixed list (Streaming, Productivity, Games, Food).
FR-ADD-07: The system shall let the user search the launchable applications installed on the device by typing in the App field. Suggestions shall be filtered by name (case-insensitive), sorted alphabetically and shown with their icons. 
FR-ADD-08: When the user selects an installed application, the system shall fill in the subscription name and associate the application’s package name with the subscription.
FR-ADD-09: The system shall allow the user to type a custom name instead of selecting an installed application; in this case no package name is associated.
FR-ADD-10: When the user taps Save, the system shall store the new subscription and its category in the local database and return to the list. 
FR-ADD-11: When the user taps Cancel, the system shall discard the entered data and return to the previous screen.

3.3 Editing a subscription (FR-EDT)
FR-EDT-01: The system shall allow the user to edit an existing subscription on the same screen as adding, pre-filled with the stored values.
FR-EDT-02: In edit mode the system shall show an Active/Inactive switch and store its value.
FR-EDT-03: In edit mode, saving shall update the existing record and shall not create a new one.
FR-EDT-04: The validation rules of FR-ADD-002 shall also apply in edit mode.


3.4 Deleting a subscription (FR-DEL)
FR-DEL-01: The system shall allow the user to delete a subscription, after which it shall no longer appear in the list.
FR-DEL-02: The system shall allow the user to preview the details of chosen subscription, such as name, price, billing period, and next renewal date.

3.5 Subscription details (FR-DET)
FR-DET-01: The system shall display the stored data of the selected subscription on a details screen and give access to editing it.

3.6 Dashboard and budget (FR-DSH)
FR-DSH-01: The system shall calculate for each subscription its most recent payment date and its next renewal date from the start date and the billing period.
FR-DSH-02: The system shall allow the user to set a target monthly budget.
FR-DSH-03: The system shall show the ratio of the current month’s subscription spending to the target budget, colored green, orange or red according to the level of spending. 

3.7 Notifications (FR-NTF)
FR-NTF-01: The system shall send a notification N days before the renewal date of a subscription, where N is the “Days before to remind” value set for that subscription.
FR-NTF-02: The system shall schedule reminders so that they are delivered at the scheduled time. [-03]
FR-NTF-03: The system shall request the usage-access permission from the user in order to find out whether the application associated with a subscription is being used. 
FR-NTF-04: The system shall send a reminder for a subscription whose associated application is not being used. 

3.8 Settings (FR-SET)
FR-SET-01: The system shall provide a settings page. 
FR-SET-02: The system shall let the user change the language. 
FR-SET-03: The system shall let the user change the date format. 
FR-SET-04: The system shall let the user change the currency. 
FR-SET-05: The system shall let the user change the theme. 
FR-SET-06: The system shall apply the chosen settings across the application.

4. External interface requirements
EIR-UI-01 Screens: subscription list (home), add/edit, details, dashboard, settings, notification settings, import. EIR-SW-01 Android permissions used: notifications, usage access, visibility of installed applications. 
EIR-HW-01: No special hardware is required.
EIR-COM-01: The application has no network interface.
5 Non-Functional Requirements
5.1 Performance Requirements (NFR-PRF)
NFR-PRF-01: The application launch time on target mobile devices shall not exceed 1–2 seconds.
NFR-PRF-02: The system shall respond to user interactive actions within 200 milliseconds to 2 seconds.
NFR-PRF-03: Local database operations executed against Room/SQLite shall complete in under 100 milliseconds.
5.2 Reliability and availability (NFR-REL)
NFR-REL-01: The system shall maintain an application availability rate of 99%, excluding scheduled maintenance windows.
NFR-REL-02: The system shall operate fully offline using a local Room SQLite database without requiring a continuous network connection.
NFR-REL-03: The system shall ensure local data persistence across app restarts and device reboots.
5.3 Security and data privacy (NFR-SEC)
NFR-SEC-01: The system shall encrypt and securely store user authentication credentials locally using the PBKDF2WithHmacSHA256 hashing algorithm.
NFR-SEC-02: All user data pertaining to spending, subscriptions, and usage stats shall remain strictly local to the user's phone.
5.4 Usability (NFR-USB)
NFR-USB-01: The system UI components shall strictly conform to the Material 3 UI/UX design specifications.
NFR-USB-02: The system shall provide selectable Dark and Light visual themes to support varying lighting conditions and user preferences.
NFR-USB-03: All interactive elements, including buttons, cards, and checkboxes, shall meet or exceed the minimum touch target dimension of 48×48 dp.
5.5 Maintainability (NFR-MNT)
NFR-MNT-01: The system codebase shall be organized following Clean Architecture principles to promote modularity and testability.
NFR-MNT-02: Background tracking services and scheduled tasks shall minimize CPU cycles and eliminate unnecessary network polling to prevent measurable battery drain.
5.6 Portability and compatibility (NFR-PRT)
NFR-PRT-01: The system shall utilize declarative Jetpack Compose layouts that adaptively scale across various mobile screen sizes and orientations.
NFR-PRT-02: The system shall support execution on devices running Android 8.0 (API level 26) or higher.
