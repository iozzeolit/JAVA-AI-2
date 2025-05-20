package Object;

import java.sql.*;
import java.util.*;
import External.*;

public class Sentence extends QDataObj {
    public int id;
    public int recordFileId;
    public Time startTime;
    public Time endTime;
    public String content;    @Override
    public int fetch(ResultSet rs) throws Exception {
        int pos = super.fetch(rs);
        id = rs.getInt(pos++);
        recordFileId = rs.getInt(pos++);
        startTime = rs.getTime(pos++);
        endTime = rs.getTime(pos++);        
        content = rs.getString(pos++);
        return pos;
    }

    @Override   
    public List<String> toList() {
        List<String> list = super.toList();
        list.add(QPiece.conv(id));
        list.add(QPiece.conv(recordFileId));
        list.add(QPiece.conv(startTime));
        list.add(QPiece.conv(endTime));
        list.add(QPiece.convN(content));
        return list;
    }
  }
