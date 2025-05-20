package Object;

import java.sql.ResultSet;
import java.util.List;

import External.*;

public class IdCounter extends QDataObj {
    public String tableName;
    public int id;

    @Override
    public int fetch(ResultSet rs) throws Exception {
        int pos = super.fetch(rs);
        tableName = rs.getString(pos++);
        id = rs.getInt(pos++);
        return pos;
    }

    @Override
    public List<String> toList() {
        List<String> list = super.toList();
        list.add(QPiece.conv(tableName));
        list.add(QPiece.conv(id));
        return list;
    }
}
