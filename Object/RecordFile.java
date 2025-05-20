package Object;

import External.*;
import java.sql.*;
import java.util.List;

public class RecordFile extends QDataObj {
    public int id;
    public String dir;

    @Override
    public int fetch(ResultSet rs) throws Exception {
        int pos = super.fetch(rs);
        id = rs.getInt(pos++);
        dir = rs.getString(pos++);
        return pos;
    }

    @Override
    public List<String> toList()
    {
        List<String> list = super.toList();
        list.add(QPiece.conv(id));
        list.add(QPiece.conv(dir));
        return list;
    }
}