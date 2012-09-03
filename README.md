# ApplicationSessionActivity

Android does not provide a native way to be notified of applications starting and stopping. This project solves that problem.

# Usage

For reference, the project includes a working example Android application composed of two Activities.

1. Copy <code>ApplicationSessionActivity</code> into your project.
2. Add the <code>android.permission.GET_TASKS</code> permission to your <code>AndroidManifest.xml</code>
3. Make all of the Activities defined in your project extend <code>ApplicationSessionActivity</code>.
4. Create an <code>ApplicationSessionActivity.SessionStartedListener</code> and <code>ApplicationSessionActivity.SessionStoppedListener</code>, and place the code you want to be run at app start and stop into their listener methods.
