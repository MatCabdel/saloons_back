package com.backend_project_template.domains.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ContactDTO {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_MESSAGE_LENGTH = 10;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String lastName;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String firstName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotNull(message = "Le sujet est obligatoire")
    private ContactSubject subject;

    @NotBlank(message = "Le message est obligatoire")
    @Size(min = MIN_MESSAGE_LENGTH, max = MAX_MESSAGE_LENGTH, message = "Le message doit contenir entre 10 et 2000 caractères")
    private String message;

    public ContactDTO() {
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ContactSubject getSubject() {
        return subject;
    }

    public void setSubject(ContactSubject subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
