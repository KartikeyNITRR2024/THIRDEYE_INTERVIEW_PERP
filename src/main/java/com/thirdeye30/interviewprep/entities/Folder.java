package com.thirdeye30.interviewprep.entities;

import com.thirdeye30.interviewprep.enums.FileType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("FOLDER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder extends Explorer {

    @Column(name = "no_of_files")
    private Integer noOfFiles = 0;

    @Column(name = "no_of_folders")
    private Integer noOfFolders = 0;
}
