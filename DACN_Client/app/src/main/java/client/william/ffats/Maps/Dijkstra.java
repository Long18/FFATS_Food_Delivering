package client.william.ffats.Maps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class Dijkstra {
    private int[] m_LuuVet;
    private int[] m_ChuaXet;
    private Double[] m_DoDaiDuongDiToi;
    int m_start;
    int m_finish;

    private int MAX_length;
    private Double[][] graph;

    //region Dijkstra function

    /**
     * get way had calculate by a
     *
     * @param vertices input vertices
     * @param findList way after get
     */
    public void getWayForDijkstra(ArrayList<Node> vertices, ArrayList<Node> findList) {
        int i = m_finish;
        while (m_LuuVet[i] != m_start) {
            findList.add(vertices.get(m_LuuVet[i]));
            i = m_LuuVet[i];
        }
    }

    public boolean runDijkstra(int start, int finish) {
        this.m_start = start;
        this.m_finish = finish;

        for (int i = 0; i < MAX_length; i++) {
            m_ChuaXet[i] = 0;
            m_DoDaiDuongDiToi[i] = Double.MAX_VALUE;
            m_LuuVet[i] = -1;
        }
        m_DoDaiDuongDiToi[start] = 0d;


        while (m_ChuaXet[finish] == 0) {
            int u = findShortestWay(MAX_length);
//            Log.e("TAG", "Dijkstra: " + u );

            if (u == -1) {
                break;
            }
            capNhatDuongDi(u, MAX_length);


        }
        if (m_ChuaXet[finish] == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void capNhatDuongDi(int u, int MAX_length) {
        m_ChuaXet[u] = 1;
        for (int i = 0; i < MAX_length; i++) {
            if (m_ChuaXet[i] == 0 && graph[u][i] > 0 && graph[u][i] < Double.MAX_VALUE) {
                if (m_DoDaiDuongDiToi[i] > m_DoDaiDuongDiToi[u] + graph[u][i]) {
                    m_DoDaiDuongDiToi[i] = m_DoDaiDuongDiToi[u];
                    m_LuuVet[i] = u;
                }
            }
        }
    }

    public int findShortestWay(int MAX_length) {
        int vertex = -1;// if can't find any Way then return -1
        double min = Double.MAX_VALUE;
        for (int i = 0; i < MAX_length; i++) {
            if (m_ChuaXet[i] == 0 && m_DoDaiDuongDiToi[i] < min) {
                min = m_DoDaiDuongDiToi[i];
                vertex = i;
            }
        }
        return vertex;
    }

    //endregion

    //region Dijkstra with priority queue

    private distNode m_dist[];
    private Set<Integer> m_settled;
    private PriorityQueue<distNode> m_pq;

    public boolean runDijkstraWithPriorityQueue(int startNode) {
        this.m_start = startNode;

        for (int i = 0; i < MAX_length; i++)
            m_dist[i] = new distNode(-1 , -1, Double.MAX_VALUE);

        // Add source node to the priority queue
        m_pq.add(new distNode(-1, startNode, 0d));

        // Distance to the source is 0
        m_dist[startNode].distance = 0d;

        while (m_settled.size() != MAX_length) {

            // Terminating ondition check when
            // the priority queue is empty, return
            if (m_pq.isEmpty())
                return true;

            // Removing the minimum distance node
            // from the priority queue
            distNode u = m_pq.remove();

            // Adding the node whose distance is
            // finalized
            if (m_settled.contains(u.destination))

                // Continue keyword skips exwcution for
                // following check
                continue;

            // We don't have to call e_Neighbors(u)
            // if u is already present in the settled set.
            m_settled.add(u.destination);

            findNeighbors(u);
        }
        return true;
    }

    // Method 2
    // To process all the neighbours
    // of the passed node
    private void findNeighbors(distNode i) {

        Double edgeDistance = -1d;
        Double newDistance = -1d;

        // All the neighbors of v
        for (int j = 0; j < MAX_length; j++) {
            //
            distNode distNode = new distNode();
            distNode.source = i.destination;
            distNode.destination = j;
            distNode.distance = graph[i.destination][j];

            if (distNode.distance == Double.MAX_VALUE){
                continue;
            }else {
                // If current node hasn't already been processed
                if (!m_settled.contains(distNode.destination)) {

                    edgeDistance = distNode.distance;
                    newDistance = m_dist[i.destination].distance + edgeDistance;

                    // If new distance is cheaper in cost
                    if (newDistance < m_dist[distNode.destination].distance){
                        m_dist[distNode.destination].distance = newDistance;
                        m_dist[distNode.destination].source = i.destination;
                    }


                    // Add the current node to the queue
                    m_pq.add(new distNode(distNode.source, distNode.destination, m_dist[distNode.destination].distance));
                }
            }
        }
    }

    public void getWayForDijkstraWithPriorityQueueToGraph(ArrayList<Node> vertices, OrderGraphItem orderGraphItem, int i_finish) {
        int finish = i_finish;
        Double distance = 0d;
        int lastSource = -1;
        boolean isFirstTime = true;
        int source = -1;
        while (m_dist[finish].source != m_start) {
            source = m_dist[finish].source;
            if (source != -1){

                orderGraphItem.getWayList().add(vertices.get(source));
                finish = m_dist[finish].source;

                if (isFirstTime){
                    isFirstTime = false;
                }else{
                    distance += graph[source][lastSource];
                }
                lastSource = source;

            }else {
                break;
            }
        }
        if (orderGraphItem.getWayList().stream().count() < 1){
            distance += graph[m_start][i_finish];
        }

        if (orderGraphItem.getWayList().stream().count() == 1){
            distance += graph[vertices.indexOf(orderGraphItem.getWayList().get(0))][i_finish];
            distance += graph[m_start][vertices.indexOf(orderGraphItem.getWayList().get(0))];
        }

        orderGraphItem.setDistance(distance);

    }

    public Double getWayForDijkstraWithPriorityQueue(ArrayList<Node> vertices, ArrayList<Node> pathList,int finish) {
        Double distance = 0d;
        int lastSource = -1;
        boolean isFirstTime = true;

        while (m_dist[finish].source != m_start) {
            int source = m_dist[finish].source;
            if (source != -1){

                pathList.add(vertices.get(source));
                finish = m_dist[finish].source;

                if (isFirstTime){
                    isFirstTime = false;
                }else{
                    distance += graph[lastSource][source];
                }
                lastSource = source;

            }else {
                break;
            }
        }

        return distance;
    }

    private class distNode implements Comparator<distNode> {
        int source;
        int destination;
        Double distance;

        // Constructors of this class

        // Constructor 1
        public distNode() {
        }

        // Constructor 2


        public distNode(int source, int destination, Double distance) {
            this.source = source;
            this.destination = destination;
            this.distance = distance;
        }

        // Method 1
        @Override
        public int compare(distNode node1, distNode node2) {

            if (node1.distance < node2.distance)
                return -1;

            if (node1.distance > node2.distance)
                return 1;

            return 0;
        }

    }

    //endregion

    //region constructor

    public Dijkstra() {

    }

    public Dijkstra(int MAX_length, Double[][] graph) {
        this.MAX_length = MAX_length;
        this.graph = graph;
        this.m_ChuaXet = new int[MAX_length];
        this.m_LuuVet = new int[MAX_length];
        this.m_DoDaiDuongDiToi = new Double[MAX_length];

        m_dist = new distNode[MAX_length];
        m_settled = new HashSet<Integer>();
        m_pq = new PriorityQueue<distNode>(MAX_length, new distNode());
    }

    //endregion

    //region getter and setter

    public int getMAX_length() {
        return MAX_length;
    }

    public void setMAX_length(int MAX_length) {
        this.MAX_length = MAX_length;
    }

    public Double[][] getGraph() {
        return graph;
    }

    public void setGraph(Double[][] graph) {
        this.graph = graph;
    }

    //endregion
}
