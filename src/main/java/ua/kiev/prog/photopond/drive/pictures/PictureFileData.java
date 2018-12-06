package ua.kiev.prog.photopond.drive.pictures;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "pictureFilesData")
public class PictureFileData implements Serializable {

    private static final long serialVersionUID = -2025907463937504632L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 5_242_880)         /*max length == 5Mb*/
    private byte[] data;

    private int size;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PictureFile pictureFile;

    public PictureFileData() {
    }

    public PictureFileData(PictureFile pictureFile, @NotNull byte[] data) {
        setData(data);
        this.pictureFile = pictureFile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.size = data.length;
    }

    public int getSize() {
        return size;
    }

    public PictureFile getPictureFile() {
        return pictureFile;
    }

    public void setPictureFile(PictureFile pictureFile) {
        this.pictureFile = pictureFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PictureFileData that = (PictureFileData) o;
        return id.equals(that.id) &&
                Arrays.equals(data, that.data) &&
                pictureFile.equals(that.pictureFile);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, pictureFile);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "PictureFileData{" +
                "id=" + id +
                ", pictureFile=" + pictureFile +
                '}';
    }
}
