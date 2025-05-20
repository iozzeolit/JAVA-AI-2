package Back;

import External.*;
import java.sql.*;;

public class IdCounterQuery {
    public static int getIdThenIncrease(String tableName) {
        Query q = new Query();
        q.select("[id]");
        q.from("[IdCounter]");
        q.where("[tableName] = '" + tableName + "'");

        var rf = new QReaderFunction() {
            public int id = 0;

            @Override
            public void Fetch(ResultSet rs) throws Exception {
                int pos = 1;
                id = rs.getInt(pos++);
            }
        };
        QDatabase.ExecQuery(q.selectQuery(), rf);
        q = new Query();
        q.update("[IdCounter]");
        q.set("[id] = [id] + 1");
        q.where("[tableName] = '" + tableName + "'");
        QDatabase.update(q);
        return rf.id;
    }
}
