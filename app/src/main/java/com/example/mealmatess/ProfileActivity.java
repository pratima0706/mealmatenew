package com.example.mealmatess;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mealmatess.models.User;
import com.example.mealmatess.utils.DataManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton backButton, uploadImageButton;
    private CircleImageView profileImageView;
    private TextView greetingText, profileNameText, profileEmailText, profilePhoneText;
    private TextInputEditText profileNameInput, profileEmailInput;
    private TextView profilePhoneEdit;
    private MaterialButton editProfileButton, saveButton, changePasswordButton, logoutButtonReadOnly, logoutButtonEdit;
    private View readOnlyView, editView;
    private DataManager dataManager;
    private Bitmap profileImage;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private String oldEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        backButton = findViewById(R.id.back_button);
        uploadImageButton = findViewById(R.id.upload_image_button);
        profileImageView = findViewById(R.id.profile_image);
        greetingText = findViewById(R.id.greeting_text);
        profileNameText = findViewById(R.id.profile_name_text);
        profileEmailText = findViewById(R.id.profile_email_text);
        profilePhoneText = findViewById(R.id.profile_phone);
        profileNameInput = findViewById(R.id.profile_name_input);
        profileEmailInput = findViewById(R.id.profile_email_input);
        profilePhoneEdit = findViewById(R.id.profile_phone_edit);
        editProfileButton = findViewById(R.id.edit_profile_button);
        saveButton = findViewById(R.id.save_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        logoutButtonReadOnly = findViewById(R.id.logout_button_read_only);
        logoutButtonEdit = findViewById(R.id.logout_button_edit);
        readOnlyView = findViewById(R.id.read_only_view);
        editView = findViewById(R.id.edit_view);
        dataManager = new DataManager(this);

        // Load current user data
        oldEmail = dataManager.getUserEmail();
        loadUserData();

        // Initialize image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            profileImage = Bitmap.createScaledBitmap(originalBitmap, 120, 120, true); // Match CircleImageView size
                            profileImageView.setImageBitmap(profileImage);
                            profileImageView.setVisibility(View.VISIBLE);
                            uploadImageButton.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set up listeners
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        editProfileButton.setOnClickListener(v -> {
            // Switch to edit mode
            readOnlyView.setVisibility(View.GONE);
            editView.setVisibility(View.VISIBLE);
            uploadImageButton.setVisibility(View.VISIBLE);
        });
        saveButton.setOnClickListener(v -> {
            String name = profileNameInput.getText().toString().trim();
            String email = profileEmailInput.getText().toString().trim();
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and email are required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // Usin current password since password change is handled separately
            User currentUserForPassword = dataManager.getUser(oldEmail);
            if (currentUserForPassword == null) {
                Toast.makeText(this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
                return;
            }
            // Updates user profile (excluding password)
            dataManager.updateUser(oldEmail, email, currentUserForPassword.getPassword(), name, profileImage);
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

            // Switch back to read-only mode and reload data
            readOnlyView.setVisibility(View.VISIBLE);
            editView.setVisibility(View.GONE);
            uploadImageButton.setVisibility(View.GONE);
            loadUserData();
        });

        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        logoutButtonReadOnly.setOnClickListener(v -> {
            dataManager.logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        logoutButtonEdit.setOnClickListener(v -> {
            dataManager.logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        User currentUser = dataManager.getUser(oldEmail);
        if (currentUser != null) {
            String fullName = currentUser.getName();
            String nickname = fullName.split(" ")[0]; // Take first name as nickname
            greetingText.setText("Hello, " + nickname + "!");
            profileNameText.setText("Name: " + fullName);
            profileEmailText.setText("Email: " + currentUser.getEmail());
            profilePhoneText.setText("Phone: (064) 456-7890"); // Placeholder
            profileNameInput.setText(fullName);
            profileEmailInput.setText(currentUser.getEmail());
            profilePhoneEdit.setText("Phone: (064) 456-7890"); // Placeholder
            if (currentUser.getImage() != null) {
                profileImageView.setImageBitmap(currentUser.getImage());
                profileImage = currentUser.getImage();
                uploadImageButton.setVisibility(View.GONE);
            } else {
                profileImage = null;
                uploadImageButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        ImageButton backButtonDialog = dialogView.findViewById(R.id.back_button);
        TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.current_password_input);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.new_password_input);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirm_password_input);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        MaterialButton confirmButton = dialogView.findViewById(R.id.confirm_button);

        AlertDialog dialog = builder.create();

        backButtonDialog.setOnClickListener(v -> dialog.dismiss());
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Validate current password
            if (!dataManager.checkUser(oldEmail, currentPassword)) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate new password length
            if (newPassword.length() < 6) {
                Toast.makeText(this, "New password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password match
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update password and log out to enforce new password
            User currentUser = dataManager.getUser(oldEmail);
            if (currentUser != null) {
                dataManager.updateUser(oldEmail, currentUser.getEmail(), newPassword, currentUser.getName(), currentUser.getImage());
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                dataManager.logout(); // Log out to enforce new password
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error updating password", Toast.LENGTH_SHORT).show();
            }
        });



        dialog.show();
    }
}