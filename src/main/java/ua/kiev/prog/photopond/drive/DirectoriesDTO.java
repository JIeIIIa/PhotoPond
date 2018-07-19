package ua.kiev.prog.photopond.drive;

import java.util.LinkedList;
import java.util.List;

public class DirectoriesDTO {
    private DriveItemDTO current;

    private DriveItemDTO parent;

    private List<DriveItemDTO> childDirectories = new LinkedList<>();

    public DriveItemDTO getParent() {
        return parent;
    }

    public void setParent(DriveItemDTO parent) {
        this.parent = parent;
    }

    public List<DriveItemDTO> getChildDirectories() {
        return childDirectories;
    }

    public void setChildDirectories(List<DriveItemDTO> childDirectories) {
        this.childDirectories = childDirectories;
    }

    public DriveItemDTO getCurrent() {
        return current;
    }

    public void setCurrent(DriveItemDTO current) {
        this.current = current;
    }
}
