package com.fsindustry.bach.core.log.file;

import lombok.ToString;

import java.io.File;

@ToString(callSuper = true)
public class NormalLogDir extends AbstractLogDir {

    NormalLogDir(File dir) {
        super(dir);
    }
}
