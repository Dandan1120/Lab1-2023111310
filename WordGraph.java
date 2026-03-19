import java.util.*;
import java.io.*;

public class WordGraph {
    // 字段 'graph' 设为 final，符合 IDE 优化建议
    private static final Map<String, Map<String, Integer>> graph = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入文本文件路径:");
        String filePath = scanner.nextLine();

        try {
 HEAD
            // 功能需求1：读入文本并生成相应的有向图 [cite: 40]

            buildGraph(filePath);
            System.out.println("有向图构建成功！");

            while (true) {
                System.out.println("\n--- 哈工大软件工程 Lab1 功能菜单 ---");
                System.out.println("1. 展示有向图 (showDirectedGraph)");
                System.out.println("2. 查询桥接词 (queryBridgeWords)");
                System.out.println("3. 根据桥接词生成新文本 (generateNewText)");
                System.out.println("4. 计算两个单词之间的最短路径 (calcShortestPath)");
                System.out.println("5. 计算 PageRank (calPageRank)");
                System.out.println("6. 随机游走 (randomWalk)");
                System.out.println("0. 退出");
                System.out.print("请选择功能编号: ");

                int choice;
                try {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // 消耗换行符
                } catch (Exception e) {
                    System.out.println("请输入数字！");
                    scanner.nextLine();
                    continue;
                }

                if (choice == 0) break;
                switch (choice) {
                    case 1:
                        showDirectedGraph(graph); // [cite: 77]
                        break;
                    case 2:
                        System.out.print("输入 word1: ");
                        String w1 = scanner.next();
                        System.out.print("输入 word2: ");
                        String w2 = scanner.next();
                        System.out.println(queryBridgeWords(w1, w2)); // [cite: 81]
                        break;
                    case 3:
                        System.out.println("请输入一行新文本:");
                        String inputText = scanner.nextLine();
                        System.out.println("生成文本: " + generateNewText(inputText)); // [cite: 113]
                        break;
                    case 4:
                        System.out.print("输入起点: ");
                        String start = scanner.next();
                        System.out.print("输入终点: ");
                        // 注意：这里可以根据手册可选功能 [cite: 153] 进一步扩展
                        String end = scanner.next();
                        System.out.println(calcShortestPath(start, end)); // [cite: 143]
                        break;
                    case 5:
                        // 按照手册要求，计算 PageRank 时 d 统一设为 0.85 [cite: 246]
                        System.out.println("全图节点 PageRank 值计算中...");
                        for (String node : graph.keySet()) {
                            System.out.println(node + ": " + calPageRank(node)); // [cite: 176]
                        }
                        break;
                    case 6:
                        System.out.println("开始随机游走...");
                        System.out.println("结果: " + randomWalk()); // [cite: 209]
                        break;
                    default:
                        System.out.println("无效选择，请重新输入。");
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
        }
    }

    // --- 功能需求1: 读入文本并生成有向图 [cite: 40, 44] ---
    public static void buildGraph(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            // 规则：换行符和非字母字符均视为空格，统一转小写 [cite: 31, 33, 45]
            sb.append(line.replaceAll("[^a-zA-Z]", " ").toLowerCase()).append(" ");
        }
        reader.close();

        String[] words = sb.toString().trim().split("\\s+");
        for (int i = 0; i < words.length - 1; i++) {
            String u = words[i];
            String v = words[i + 1];
            if (u.isEmpty() || v.isEmpty()) continue;

            // 权重 w 为 A->B 相邻出现的次数 [cite: 47]
            graph.computeIfAbsent(u, k -> new HashMap<>())
                    .merge(v, 1, Integer::sum);
        }
    }

// --- 功能需求2: 展示有向图 (命令行展示 + 生成可视化图片) ---
    public static void showDirectedGraph(Map<String, Map<String, Integer>> G) {
        // 1. 命令行输出邻接表
        System.out.println("\n--- 有向图结构 ---");
        for (var entry : G.entrySet()) {
            System.out.print(entry.getKey() + " -> ");
            List<String> list = new ArrayList<>();
            entry.getValue().forEach((k, v) -> list.add(k + "(" + v + ")"));
            System.out.println(String.join(", ", list));
        }

        // 2. 准备 DOT 文件内容
        StringBuilder dotContent = new StringBuilder();
        dotContent.append("digraph G {\n");
        dotContent.append("    node [shape=ellipse];\n");

        for (Map.Entry<String, Map<String, Integer>> entry : G.entrySet()) {
            String u = entry.getKey();
            for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                String v = edge.getKey();
                int weight = edge.getValue();
                // 格式: "word1" -> "word2" [label="weight"];
                dotContent.append(String.format("    \"%s\" -> \"%s\" [label=\"%d\"];\n", u, v, weight));
            }
        }
        dotContent.append("}");

        // 3. 将内容写入 graph.dot
        try (PrintWriter out = new PrintWriter("graph.dot")) {
            out.println(dotContent.toString());
            System.out.println("DOT文件已生成。");
        } catch (IOException e) {
            System.err.println("文件写入失败: " + e.getMessage());
        }

        // 4. 调用 Graphviz 生成 PNG 图片
        try {
            Process process = Runtime.getRuntime().exec("dot -Tpng graph.dot -o graph.png");
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("有向图图片已生成: graph.png，请在项目根目录下查看。");
            } else {
                System.out.println("提示：Graphviz 执行失败，请确保 dot 命令在系统 Path 中。");
            }
        } catch (Exception e) {
            System.out.println("执行绘图命令时出错。已回退至控制台展示：");
            System.out.println(dotContent.toString());
        }
    }
    // --- 功能需求3: 查询桥接词 [cite: 81] ---
    public static String queryBridgeWords(String word1, String word2) {
        String w1 = word1.toLowerCase();
        String w2 = word2.toLowerCase();

        boolean hasW1 = graph.containsKey(w1);
        boolean hasW2 = graph.containsKey(w2) || graph.values().stream().anyMatch(m -> m.containsKey(w2));

        if (!hasW1 && !hasW2) return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
        if (!hasW1) return "No \"" + word1 + "\" in the graph!";
        if (!hasW2) return "No \"" + word2 + "\" in the graph!";

        List<String> bridges = new ArrayList<>();
        Map<String, Integer> nextWords = graph.get(w1);
        if (nextWords != null) {
            for (String bridge : nextWords.keySet()) {
                if (graph.containsKey(bridge) && graph.get(bridge).containsKey(w2)) {
                    bridges.add(bridge);
                }
            }
        }

        if (bridges.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: " +
                    String.join(", ", bridges) + ".";
        }
    }

    // --- 功能需求4: 根据桥接词生成新文本 [cite: 113] ---
    public static String generateNewText(String inputText) {
        // 分割单词时保留字母逻辑 [cite: 114]
        String[] words = inputText.split("\\s+");
        if (words.length < 2) return inputText;

        StringBuilder result = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < words.length - 1; i++) {
            String w1 = words[i].toLowerCase().replaceAll("[^a-zA-Z]", "");
            String w2 = words[i + 1].toLowerCase().replaceAll("[^a-zA-Z]", "");
            result.append(words[i]).append(" ");

            List<String> bridges = new ArrayList<>();
            if (graph.containsKey(w1)) {
                for (String bridge : graph.get(w1).keySet()) {
                    if (graph.containsKey(bridge) && graph.get(bridge).containsKey(w2)) {
                        bridges.add(bridge);
                    }
                }
            }

            if (!bridges.isEmpty()) {
                // 如果有多个桥接词，随机选一个 [cite: 117]
                String chosen = bridges.get(rand.nextInt(bridges.size()));
                result.append(chosen).append(" ");
            }
        }
        result.append(words[words.length - 1]);
        return result.toString();
    }

    // --- 功能需求5: 计算两个单词之间的最短路径 ---
    public static String calcShortestPath(String word1, String word2) {
        String start = word1.toLowerCase();
        String end = word2.toLowerCase();

        // 检查单词是否在图中 [cite: 152]
        if (!graph.containsKey(start)) return "不可达：单词 \"" + word1 + "\" 不在图中！";

        // 检查 end 是否作为任何边的终点存在
        boolean endExists = graph.containsKey(end) || graph.values().stream().anyMatch(m -> m.containsKey(end));
        if (!endExists) return "不可达：单词 \"" + word2 + "\" 不在图中！";

        // Dijkstra 算法初始化
        Map<String, Integer> dist = new HashMap<>(); // 存储到起点的最短距离
        Map<String, String> prev = new HashMap<>(); // 记录路径前驱
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        // 初始化所有已知节点的距离为无穷大
        Set<String> nodes = new HashSet<>(graph.keySet());
        graph.values().forEach(m -> nodes.addAll(m.keySet()));
        for (String node : nodes) dist.put(node, Integer.MAX_VALUE);

        dist.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (u.equals(end)) break; // 找到终点，提前退出

            Map<String, Integer> neighbors = graph.get(u);
            if (neighbors == null) continue;

            for (var entry : neighbors.entrySet()) {
                String v = entry.getKey();
                int weight = entry.getValue();
                int alt = dist.get(u) + weight; // 边权值之和最小 [cite: 143]
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        // 检查是否可达 [cite: 152]
        if (dist.get(end) == Integer.MAX_VALUE) {
            return "从 \"" + word1 + "\" 到 \"" + word2 + "\" 不可达！";
        }

        // 构建路径字符串
        LinkedList<String> pathList = new LinkedList<>();
        for (String at = end; at != null; at = prev.get(at)) {
            pathList.addFirst(at);
        }

        return "最短路径: " + String.join(" -> ", pathList) + "\n路径长度: " + dist.get(end);
    }
    // --- 功能需求6: 计算PageRank ---
    public static Double calPageRank(String word) {
        String target = word.toLowerCase();
        Set<String> allNodes = new HashSet<>(graph.keySet());
        graph.values().forEach(m -> allNodes.addAll(m.keySet()));

        if (!allNodes.contains(target)) return 0.0;

        int n = allNodes.size();
        double d = 0.85; // 实验要求阻尼因子统一设定为0.85
        Map<String, Double> pr = new HashMap<>();

        // 初始化PR值
        for (String node : allNodes) pr.put(node, 1.0 / n);

        // 迭代计算（通常迭代100次可趋于稳定）
        for (int iter = 0; iter < 100; iter++) {
            Map<String, Double> nextPr = new HashMap<>();
            double sinkSum = 0; // 处理出度为0的节点

            for (String node : allNodes) {
                nextPr.put(node, (1 - d) / n);
                if (!graph.containsKey(node) || graph.get(node).isEmpty()) {
                    sinkSum += d * (pr.get(node) / n);
                }
            }

            for (String u : graph.keySet()) {
                Map<String, Integer> edges = graph.get(u);
                double outDegree = edges.size();
                for (String v : edges.keySet()) {
                    nextPr.put(v, nextPr.get(v) + d * (pr.get(u) / outDegree));
                }
            }

            // 将沉没节点的PR值均分给所有节点
            for (String node : allNodes) {
                nextPr.put(node, nextPr.get(node) + sinkSum);
            }
            pr = nextPr;
        }
        return pr.get(target);
    }

    // --- 功能需求7: 随机游走 ---
    public static String randomWalk() {
        List<String> nodes = new ArrayList<>(graph.keySet());
        if (nodes.isEmpty()) return "图为空！";

        Random rand = new Random();
        String current = nodes.get(rand.nextInt(nodes.size()));
        StringBuilder walk = new StringBuilder(current);
        Set<String> visitedEdges = new HashSet<>();

        while (true) {
            Map<String, Integer> neighbors = graph.get(current);
            // 进入的某个节点不存在出边为止
            if (neighbors == null || neighbors.isEmpty()) break;

            List<String> nextOptions = new ArrayList<>(neighbors.keySet());
            String next = nextOptions.get(rand.nextInt(nextOptions.size()));
            String edge = current + "->" + next;

            // 直到出现第一条重复的边为止
            if (visitedEdges.contains(edge)) {
                walk.append(" ").append(next);
                break;
            }

            visitedEdges.add(edge);
            walk.append(" ").append(next);
            current = next;

            // 用户也可随时停止遍历（在命令行环境下通常简化为自动执行，或添加特定的停止逻辑）
        }

        String result = walk.toString();
        // 将遍历的节点输出为文本，并以文件形式写入磁盘
        try (PrintWriter out = new PrintWriter("random_walk.txt")) {
            out.println(result);
            System.out.println("随机游走路径已保存至 random_walk.txt");
        } catch (IOException e) {
            System.err.println("文件写入失败: " + e.getMessage());
        }
        return result;
    }
}