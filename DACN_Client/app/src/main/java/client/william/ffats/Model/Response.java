package client.william.ffats.Model;

import java.util.List;

public class Response {
    public long multicast_id;
    public int success,failure,canonical_ids;
    public List<Result> result;
}
