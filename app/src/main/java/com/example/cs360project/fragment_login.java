package com.example.cs360project;
import android.app.AlertDialog;
import android.text.InputType;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;

public class fragment_login extends Fragment {
    private static final String TAG = "fragment_login";
    private static final String ARG_DB_HELPER = "dbHelper";
    private static final int SMS_PERMISSION_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private OnFragmentInteractionListener mListener;
    private DatabaseHelper dbHelper;


    public interface OnFragmentInteractionListener {
        void onLoginSuccess(String username);
    }

    public static fragment_login newInstance(DatabaseHelper dbHelper) {
        fragment_login fragment = new fragment_login();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DB_HELPER, dbHelper);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dbHelper = (DatabaseHelper) getArguments().getSerializable(ARG_DB_HELPER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button button = view.findViewById(R.id.login);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null && dbHelper != null) {
                    EditText etUsername = view.findViewById(R.id.username);
                    EditText etPassword = view.findViewById(R.id.password);
                    String username = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    if (dbHelper.checkUser(username, password)) {
                        // Login successful, navigate to fragment_item_list
                        mListener.onLoginSuccess(username);
                    } else {
                        // Login failed, show registration dialog
                        showRegistrationDialog(username);
                    }

                }
                else {
                    Log.d(TAG, "NULL LISTENER!!!");
                }
            }
        });

        Button smsPermissionButton = view.findViewById(R.id.btn_request_sms_permission);
        smsPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions();
            }
        });

        return view;
    }

    private void requestPermissions() {
        String[] permissions = {Manifest.permission.SEND_SMS};
        boolean shouldRequest = false;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                shouldRequest = true;
                break;
            }
        }

        if (shouldRequest) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(getContext(), "All required permissions are already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(getContext(), "All permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Some permissions were denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void showRegistrationDialog(final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(String.format("Login Credentials Not Found \nCreate New Account for '%s'", username));

        // Set up the input
        final EditText input = new EditText(getContext());
        final EditText confirmInput = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter password");
        confirmInput.setHint("Confirm password");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(input);
        layout.addView(confirmInput);
        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                String confirmPassword = confirmInput.getText().toString();

                if (password.equals(confirmPassword)) {
                    if (dbHelper.addUser(username, password)) {
                        Toast.makeText(getContext(), "Account created successfully! You may now sign in!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to create account", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        } else {
            Toast.makeText(requireContext(), "SMS permission already granted", Toast.LENGTH_SHORT).show();
        }
    }
}
