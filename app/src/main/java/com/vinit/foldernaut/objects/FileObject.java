package com.vinit.foldernaut.objects;

import android.support.annotation.NonNull;

import java.util.Date;

public class FileObject implements Comparable<FileObject> {

    int imageResource;
    int childCount;
    String filename, filepath;
    boolean isDirectory;
    boolean isChecked;
    Date dateCreated;

    public FileObject(String filename, String filepath, int imageResource, boolean isDirectory,
                      int childCount, boolean isChecked, Date dateCreated) {
        this.filename = filename;
        this.filepath = filepath;
        this.imageResource = imageResource;
        this.isDirectory = isDirectory;
        this.childCount = childCount;
        this.isChecked = isChecked;
        this.dateCreated = dateCreated;

    }

    public String getFilepath() {
        return filepath;
    }

    public int getImageResource() {
        return imageResource;
    }

    public boolean isDirectory() { return isDirectory; }

    public String getFilename() { return filename; }

    public int getChildCount() { return childCount; }

    public boolean isSelected() {
        return isChecked;
    }

    public Date getDateCreated() { return dateCreated; }

    public void setSelected(boolean checkedState) {
        isChecked = checkedState;
    }

    @Override
    public int compareTo(@NonNull FileObject fileObject) {

        return this.getFilename().compareToIgnoreCase(fileObject.getFilename());
    }
}
