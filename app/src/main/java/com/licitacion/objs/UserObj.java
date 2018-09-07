package com.licitacion.objs;

import java.io.Serializable;
import java.sql.Timestamp;

public class UserObj implements Serializable {

    public int id;
    public String name, email, pass, fingerPath, picPath;
}
