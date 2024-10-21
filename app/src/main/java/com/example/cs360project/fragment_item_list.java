package com.example.cs360project;

import android.os.Bundle;
import android.Manifest;
import androidx.core.util.Pair;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.database.Cursor;
import android.view.Gravity;
import java.util.Locale;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;
import android.widget.Button;
import android.widget.Spinner;
import android.telephony.SmsManager;
import androidx.annotation.NonNull;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.widget.RelativeLayout;
import android.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatButton;
import android.content.Context;
import android.graphics.Typeface;

public class fragment_item_list extends Fragment {

    private static final String ARG_USERNAME = "username";
    private static final String ARG_DB_HELPER = "dbHelper";
    private String username;
    private DatabaseHelper dbHelper;
    private EditText etGoalWeight;
    private Spinner spinnerComparison;
    private static final int SMS_PERMISSION_CODE = 100;
    private float lastAddedWeight;
    private static final int PHONE_STATE_PERMISSION_CODE = 101;
    private float goalWeight;
    private float currentWeight;
    private boolean isEditMode = false;
    private Cursor cursor;

    public fragment_item_list() {
        // Required empty public constructor
    }

    public static fragment_item_list newInstance(String username, DatabaseHelper dbHelper) {
        fragment_item_list fragment = new fragment_item_list();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        args.putSerializable(ARG_DB_HELPER, dbHelper);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
            dbHelper = (DatabaseHelper) getArguments().getSerializable(ARG_DB_HELPER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Populate the table with weight data
        populateWeightTable(view);

        // Set up FAB click listener
        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddWeightDialog();
            }
        });

        // Set up Clear Data button click listener
        Button btnClearData = view.findViewById(R.id.btn_clear_data);
        btnClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearDataConfirmationDialog();
            }
        });

        Button editDataButton = view.findViewById(R.id.btn_edit_data);
        editDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditMode();
            }
        });

        etGoalWeight = view.findViewById(R.id.et_goal_weight_val);
        spinnerComparison = view.findViewById(R.id.spinner_filter);

        // Set default goal weight and comparison
        Pair<Float, String> goalData = dbHelper.getGoalWeight(username);
        if (goalData.first != 0f) {
            etGoalWeight.setText(String.format(Locale.US, "%.2f", goalData.first));
            int comparisonIndex = getComparisonIndex(goalData.second);
            spinnerComparison.setSelection(comparisonIndex);
        } else {
            // Set default to "less than" if no previous data
            spinnerComparison.setSelection(0);
        }

        refreshWeightData();
        populateWeightTable(view);
        return view;
    }

    private void refreshWeightData() {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        cursor = dbHelper.getWeightEntries(username);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            populateWeightTable(getView());
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        populateWeightTable(getView());
    }

    private void populateWeightTable(View view) {
        TableLayout tableLayout = view.findViewById(R.id.table_weights);
        tableLayout.removeAllViews();

        if (cursor == null || cursor.isClosed()) {
            refreshWeightData();
        }

        if (cursor == null) {
            return;
        }

        int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.getColumnId());
        int weightColumnIndex = cursor.getColumnIndex(DatabaseHelper.getColumnWeight());
        int dateColumnIndex = cursor.getColumnIndex(DatabaseHelper.getColumnDate());

        if (idColumnIndex < 0 || weightColumnIndex < 0 || dateColumnIndex < 0) {
            return;
        }

        boolean isLightVariant = true;

        if (cursor.moveToLast()) {
            int totalEntries = cursor.getCount();
            int position = 0;
            do {
                TableRow row = new TableRow(getContext());
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                RelativeLayout entryLayout = new RelativeLayout(getContext());
                TableRow.LayoutParams entryParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                entryParams.setMargins(25, 25, 25, 25); // Add vertical margins for separation
                entryLayout.setLayoutParams(entryParams);

                // Set the background drawable
                int drawableResId = isLightVariant ? R.drawable.rounded_rectangle_light : R.drawable.rounded_rectangle_dark;
                entryLayout.setBackground(ContextCompat.getDrawable(getContext(), drawableResId));

                TextView weightView = createTextView(String.format(Locale.US, "  %.2f", cursor.getFloat(weightColumnIndex)), RelativeLayout.ALIGN_PARENT_START);
                TextView dateView = createTextView(String.format(Locale.US, "%s  ", cursor.getString(dateColumnIndex)), RelativeLayout.ALIGN_PARENT_END);

                entryLayout.addView(weightView);
                entryLayout.addView(dateView);

                if (isEditMode) {
                    // Create a ContextThemeWrapper with the button style from your theme
                    Context themedContext = new ContextThemeWrapper(getContext(), R.style.Widget_CS360Project_Button);

                    // Use AppCompatButton instead of Button for better compatibility
                    AppCompatButton editButton = new AppCompatButton(themedContext);

                    editButton.setText(R.string.editButton);
                    RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    buttonParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    editButton.setLayoutParams(buttonParams);
                    editButton.setTag(totalEntries - position - 1);
                    editButton.setOnClickListener(this::onEditButtonClick);
                    entryLayout.addView(editButton);
                }

                row.addView(entryLayout);
                tableLayout.addView(row);
                position++;

                // Toggle the variant for the next entry
                isLightVariant = !isLightVariant;
            } while (cursor.moveToPrevious());
        } else {
            TextView emptyView = new TextView(getContext());
            emptyView.setText(R.string.no_weight_entries_found);
            emptyView.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(0, 50, 0, 50);
            tableLayout.addView(emptyView);
        }
    }


    private TextView createTextView(String text, int alignRule) {
        TextView textView = new TextView(getContext());
        textView.setText(text);

        // Apply the TextAppearance
        textView.setTextAppearance(getContext(), R.style.TextAppearance_CS360Project_Headline2);

        // Set text color explicitly (in case it's not set in the style)
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.primary));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(alignRule);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        textView.setLayoutParams(params);

        return textView;
    }

    private void onEditButtonClick(View v) {
        int position = (int) v.getTag();
        if (cursor != null && !cursor.isClosed() && cursor.moveToPosition(position)) {
            int idColumnIndex = cursor.getColumnIndex(DatabaseHelper.getColumnId());
            int weightColumnIndex = cursor.getColumnIndex(DatabaseHelper.getColumnWeight());

            if (idColumnIndex >= 0 && weightColumnIndex >= 0) {
                int id = cursor.getInt(idColumnIndex);
                float weight = cursor.getFloat(weightColumnIndex);
                showEditDialog(id, weight);
            } else {
                Toast.makeText(getContext(), "Error: Unable to edit entry", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Optionally, refresh the data if the cursor is closed or null
            refreshWeightData();
            populateWeightTable(getView());
        }
    }

    private void showEditDialog(final int entryId, float currentWeight) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Entry");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.format(Locale.US, "%.2f", currentWeight));
        builder.setView(input);

        builder.setPositiveButton("Apply Changes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newWeightStr = input.getText().toString();
                if (!newWeightStr.isEmpty()) {
                    try {
                        float newWeight = Float.parseFloat(newWeightStr);
                        updateWeightEntry(entryId, newWeight);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid weight format", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteWeightEntry(entryId);
            }
        });

        builder.show();
    }

    private void updateWeightEntry(int entryId, float newWeight) {
        if (dbHelper.updateWeight(entryId, newWeight)) {
            Toast.makeText(getContext(), "Weight updated successfully", Toast.LENGTH_SHORT).show();
            isEditMode = false;
            refreshWeightData();
            populateWeightTable(getView());
        } else {
            Toast.makeText(getContext(), "Failed to update weight", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteWeightEntry(int entryId) {
        if (dbHelper.deleteWeight(entryId)) {
            Toast.makeText(getContext(), "Entry deleted successfully", Toast.LENGTH_SHORT).show();
            isEditMode = false;
            refreshWeightData();
            populateWeightTable(getView());
        } else {
            Toast.makeText(getContext(), "Failed to delete entry", Toast.LENGTH_SHORT).show();
        }

    }

    private void showAddWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Input Weight");

        // Set up the input
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter weight");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", null); // We'll set the click listener later
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override the OK button to check for valid input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightStr = input.getText().toString().trim();
                if (!weightStr.isEmpty()) {
                    try {
                        float weight = Float.parseFloat(weightStr);
                        addWeightEntry(weight);
                        saveGoalWeight();
                        checkGoalAchievement(weight);
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        input.setError("Please enter a valid number");
                    }
                } else {
                    input.setError("Weight cannot be empty");
                }
            }
        });
    }

    private void addWeightEntry(float weight) {
        if (dbHelper.addWeight(username, weight)) {
            Toast.makeText(getContext(), "Weight added successfully", Toast.LENGTH_SHORT).show();
            refreshWeightData();
            populateWeightTable(getView()); // Refresh the table
        } else {
            Toast.makeText(getContext(), "Failed to add weight", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGoalWeight() {
        String goalWeightStr = etGoalWeight.getText().toString().trim();
        if (!goalWeightStr.isEmpty()) {
            float goalWeight = Float.parseFloat(goalWeightStr);
            String comparison = spinnerComparison.getSelectedItem().toString();
            dbHelper.saveGoalWeight(username, goalWeight, comparison);
        }
    }

    private void checkGoalAchievement(float currWeight) {
        Pair<Float, String> goalData = dbHelper.getGoalWeight(username);
        goalWeight = goalData.first;
        currentWeight = currWeight;
        String comparison = goalData.second;

        boolean goalAchieved = false;
        switch (comparison) {
            case "less than":
                goalAchieved = currentWeight < goalWeight;
                break;
            case "equal to":
                goalAchieved = Math.abs(currentWeight - goalWeight) < 0.01; // Allow small float difference
                break;
            case "greater than":
                goalAchieved = currentWeight > goalWeight;
                break;
        }

        if (goalAchieved) {
            Toast.makeText(getContext(), "Congratulations! You've reached your goal!", Toast.LENGTH_LONG).show();
            checkSmsPermissionAndSend();
        }
    }

    private int getComparisonIndex(String comparison) {
        String[] options = getResources().getStringArray(R.array.comparison_options);
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(comparison)) {
                return i;
            }
        }
        return 0; // Default to "less than"
    }

    private void showClearDataConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Clear All Data");
        builder.setMessage("Are you sure you want to delete all your weight entries? This action cannot be undone.");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearUserData();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void clearUserData() {
        if (dbHelper.deleteAllWeightEntries(username)) {
            Toast.makeText(getContext(), "All weight entries deleted", Toast.LENGTH_SHORT).show();
            refreshWeightData();
            populateWeightTable(getView()); // Refresh the table
        } else {
            Toast.makeText(getContext(), "Failed to delete weight entries", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSmsPermissionAndSend() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            sendCongratulatorySms();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        }
    }

    private void sendCongratulatorySms() {
        String phoneNumber = "1234567890"; // Replace with actual phone number
        String message = String.format(Locale.US,
                "Congratulations! You've reached your weight goal! Goal: %.2f, Current: %.2f",
                goalWeight, currentWeight);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "SMS sending failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendCongratulatorySms();
            } else {
                Toast.makeText(getContext(), "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
