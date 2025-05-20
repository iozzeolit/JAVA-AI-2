package External;

import java.sql.*;
import java.util.*;

public abstract class QDataObj {
    public int fetch(ResultSet rs) throws Exception {
        return 0;
    }

    public List<String> toList() {
        return new ArrayList<String>();
    }

    public String toString() {
        List<String> list = toList();
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            result += list.get(i);
            if (i < list.size() - 1) {
                result += ", ";
            }
        }
        return result;
    }
}
