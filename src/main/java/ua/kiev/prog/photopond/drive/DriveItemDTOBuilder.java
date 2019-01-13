package ua.kiev.prog.photopond.drive;

import java.util.Date;

public class DriveItemDTOBuilder {
    private String name;
    private String parentUri;
    private DriveItemType type;
    private Date creationDate;

    private DriveItemDTOBuilder() {
    }

    public static DriveItemDTOBuilder getInstance() {
        return new DriveItemDTOBuilder();
    }

    public DriveItemDTOBuilder name(String name) {
        this.name = name;

        return this;
    }


    public DriveItemDTOBuilder parentUri(String parentUri) {
        this.parentUri = parentUri;

        return this;
    }

    public DriveItemDTOBuilder type(DriveItemType type) {
        this.type = type;

        return this;
    }

    public DriveItemDTOBuilder creationDate(Date creationDate) {
        this.creationDate = creationDate;

        return this;
    }

    public DriveItemDTO build() {
        DriveItemDTO driveItemDTO = new DriveItemDTO();
        driveItemDTO.setName(name);
        driveItemDTO.setParentUri(parentUri);
        driveItemDTO.setType(type);
        driveItemDTO.setCreationDate(creationDate);

        return driveItemDTO;
    }
}
