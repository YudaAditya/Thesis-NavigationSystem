object FloorGraph {
    val floorGraph = mapOf(
        1 to listOf(FloorNode(1, Position(0.0, 0.0)), FloorNode(2, Position(1.0, 0.0)), FloorNode(3, Position(2.0, 0.0))),
        2 to listOf(FloorNode(4, Position(0.0, 0.0)), FloorNode(5, Position(1.0, 0.0)), FloorNode(6, Position(2.0, 0.0)))
    )

    fun findShortestPath(startNodeId: Int, targetNodeId: Int): List<FloorNode> {
        // Implementasikan algoritma A* untuk mencari jalur terpendek dari startNodeId ke targetNodeId
        val openList = mutableListOf<FloorNode>()
        val closedList = mutableListOf<FloorNode>()
        val parentMap = mutableMapOf<FloorNode, FloorNode>()
        val gScore = mutableMapOf<FloorNode, Double>()
        val fScore = mutableMapOf<FloorNode, Double>()

        // Inisialisasi gScore dan fScore untuk semua node dengan nilai tak terhingga
        for (floorNodes in floorGraph.values) {
            for (node in floorNodes) {
                gScore[node] = Double.POSITIVE_INFINITY
                fScore[node] = Double.POSITIVE_INFINITY
            }
        }

        val startNode = floorGraph.values.flatten().first { it.id == startNodeId }
        val targetNode = floorGraph.values.flatten().first { it.id == targetNodeId }
        gScore[startNode] = 0.0
        fScore[startNode] = heuristicCostEstimate(startNode, targetNode)

        openList.add(startNode)

        while (openList.isNotEmpty()) {
            val current = openList.minByOrNull { fScore[it] ?: Double.POSITIVE_INFINITY } ?: break
            if (current == targetNode) {
                return reconstructPath(parentMap, current)
            }

            openList.remove(current)
            closedList.add(current)

            for (neighbor in getNeighbors(current)) {
                if (neighbor in closedList) {
                    continue
                }

                val tentativeGScore = gScore[current]!! + distanceBetween(current, neighbor)

                if (neighbor !in openList) {
                    openList.add(neighbor)
                } else if (tentativeGScore >= gScore[neighbor]!!) {
                    continue
                }

                parentMap[neighbor] = current
                gScore[neighbor] = tentativeGScore
                fScore[neighbor] = gScore[neighbor]!! + heuristicCostEstimate(neighbor, targetNode)
            }
        }

        return emptyList()
    }

    private fun getNeighbors(node: FloorNode): List<FloorNode> {
        return floorGraph.values.flatten().filter { it.id != node.id }
    }

    private fun heuristicCostEstimate(node1: FloorNode, node2: FloorNode): Double {
        // Implementasikan fungsi estimasi jarak heuristik antara dua node
        val deltaX = node2.position.x - node1.position.x
        val deltaY = node2.position.y - node1.position.y
        return sqrt(deltaX.pow(2) + deltaY.pow(2))
    }

    private fun distanceBetween(node1: FloorNode, node2: FloorNode): Double {
        // Implementasikan fungsi untuk menghitung jarak antara dua node
        val deltaX = node2.position.x - node1.position.x
        val deltaY = node2.position.y - node1.position.y
        return sqrt(deltaX.pow(2) + deltaY.pow(2))
    }

    private fun reconstructPath(parentMap: Map<FloorNode, FloorNode>, current: FloorNode): List<FloorNode> {
        val path = mutableListOf<FloorNode>()
        var node = current
        while (node in parentMap) {
            path.add(node)
            node = parentMap[node]!!
        }
        path.add(node)
        return path.reversed()
    }
}
