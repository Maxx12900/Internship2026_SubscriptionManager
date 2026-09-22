# Implementation Plan - Fully Functional Subscription Details Screen

This plan outlines the steps to connect the Subscription Details screen to the data layer, implement navigation, and display real subscription data.

## User Review Required

> [!IMPORTANT]
> Since the project does not yet have a Dependency Injection (DI) framework like Hilt, I will implement a manual ViewModel Factory in the `NavGraph` to inject the `SubscriptionRepository`. This is a standard approach for smaller projects.

## Proposed Changes

### Data Layer

#### [MODIFY] [SubscriptionRepository.kt](file:///C:/Users/Lenovo Legion 5 Pro/Desktop/Intership/Internship_SubscriptionManager/app/src/main/java/com/example/subscriptionmanager/data/repository/SubscriptionRepository.kt)
*   Ensure it provides all necessary methods for the details screen (already mostly there).

### ViewModels

#### [MODIFY] [SubscriptionDetailsViewModel.kt](file:///C:/Users/Lenovo Legion 5 Pro/Desktop/Intership/Internship_SubscriptionManager/app/src/main/java/com/example/subscriptionmanager/ui/subscriptionDetails/SubscriptionDetailsViewModel.kt)
*   Update constructor to accept `SubscriptionRepository`.
*   Implement `loadSubscription(id: Int)` using the repository.
*   Implement `deleteSubscription(onSuccess: () -> Unit)` using the repository.

### UI Screens

#### [MODIFY] [SubscriptionDetailsScreen.kt](file:///C:/Users/Lenovo Legion 5 Pro/Desktop/Intership/Internship_SubscriptionManager/app/src/main/java/com/example/subscriptionmanager/ui/subscriptionDetails/SubscriptionDetailsScreen.kt)
*   Update parameter list to include `SubscriptionDetailsViewModel`.
*   Call `viewModel.loadSubscription(id)` in a `LaunchedEffect`.
*   Observe `viewModel.subscription` and replace hardcoded data with actual values from the entity.

#### [MODIFY] [SubscriptionListScreen.kt](file:///C:/Users/Lenovo Legion 5 Pro/Desktop/Intership/Internship_SubscriptionManager/app/src/main/java/com/example/subscriptionmanager/ui/subscriptionList/SubscriptionListScreen.kt)
*   Update `SubscriptionCard` to accept a click listener.
*   Pass the click listener from `SubscriptionListScreen` to navigate to the details route.

### Navigation

#### [MODIFY] [NavGraph.kt](file:///C:/Users/Lenovo Legion 5 Pro/Desktop/Intership/Internship_SubscriptionManager/app/src/main/java/com/example/subscriptionmanager/navigation/NavGraph.kt)
*   Fix the `Screen.SubscriptionDetails.createRoute` bug.
*   Add the `composable` destination for the details screen.
*   Extract the `id` from the navigation arguments.
*   Instantiate `SubscriptionDetailsViewModel` with a factory that provides the `SubscriptionRepository`.

## Verification Plan

### Automated Tests
*   N/A (Current focus is on implementation and manual verification).

### Manual Verification
1.  Run the app.
2.  Add a subscription (if possible, otherwise check existing ones).
3.  Click on a subscription in the list.
4.  Verify that the details screen opens and shows the correct information (name, price, etc.).
5.  Test the "Delete" button and verify it returns to the list and the item is gone.
