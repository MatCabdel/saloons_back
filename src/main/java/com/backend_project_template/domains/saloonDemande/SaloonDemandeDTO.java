package com.backend_project_template.domains.saloonDemande;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SaloonDemandeDTO {

    private static final int MIN_PLACE_NAME_LENGTH = 2;
    private static final int MAX_PLACE_NAME_LENGTH = 255;
    private static final int MIN_ADDRESS_LENGTH = 5;
    private static final int MAX_ADDRESS_LENGTH = 500;
    private static final int MAX_COMMENT_LENGTH = 1000;

    @NotBlank(message = "Le nom du lieu est obligatoire")
    @Size(min = MIN_PLACE_NAME_LENGTH, max = MAX_PLACE_NAME_LENGTH, message = "Le nom du lieu doit contenir entre 2 et 255 caractères")
    private String placeName;

    @NotNull(message = "Le type de lieu est obligatoire")
    private PlaceType placeType;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(min = MIN_ADDRESS_LENGTH, max = MAX_ADDRESS_LENGTH, message = "L'adresse doit contenir entre 5 et 500 caractères")
    private String address;

    @Size(max = MAX_COMMENT_LENGTH, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String comment;

    public SaloonDemandeDTO() {
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
