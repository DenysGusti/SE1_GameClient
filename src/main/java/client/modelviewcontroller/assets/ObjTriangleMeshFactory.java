package client.modelviewcontroller.assets;

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

import java.util.ArrayList;
import java.util.List;

public class ObjTriangleMeshFactory {
    public TriangleMesh createTriangleMesh(String objContent) {
        if (objContent == null)
            throw new IllegalArgumentException("objContent is null");

        var triangleMesh = new TriangleMesh(VertexFormat.POINT_NORMAL_TEXCOORD);

        List<Integer> faceIndices = new ArrayList<>();

        objContent.lines().map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .forEach(line -> {
                    String[] parts = line.split("\\s+");
                    switch (parts[0]) {
                        case "v" -> {
                            float x = f(parts[1]);
                            float y = f(parts[2]);
                            float z = f(parts[3]);
                            triangleMesh.getPoints().addAll(z, -y, x);  // JavaFX coordinate system
                        }
                        case "vn" -> {
                            float x = f(parts[1]);
                            float y = f(parts[2]);
                            float z = f(parts[3]);
                            triangleMesh.getNormals().addAll(z, -y, x);  // JavaFX coordinate system
                        }
                        case "vt" -> {
                            float u = f(parts[1]);
                            float v = f(parts[2]);
                            triangleMesh.getTexCoords().addAll(u, 1 - v);  // JavaFX coordinate system
                        }
                        case "f" -> parseFace(parts, faceIndices);
                    }
                });

        int[] facesArray = faceIndices.stream().mapToInt(i -> i).toArray();
        triangleMesh.getFaces().setAll(facesArray);

        return triangleMesh;
    }

    private static void parseFace(String[] faceParts, List<Integer> faceIndices) {
        if (faceParts == null)
            throw new IllegalArgumentException("faceParts is null");
        if (faceIndices == null)
            throw new IllegalArgumentException("faceIndices is null");

        // OBJ face format: f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
        for (int i = 1; i <= 3; ++i) {
            String[] split = faceParts[i].split("/");   // v/vt/vn

            int vIdx = Integer.parseInt(split[0]) - 1;
            int vtIdx = split.length > 1 && !split[1].isEmpty() ? Integer.parseInt(split[1]) - 1 : 0;
            int vnIdx = split.length > 2 && !split[2].isEmpty() ? Integer.parseInt(split[2]) - 1 : 0;

            // JavaFX POINT_NORMAL_TEXCOORD order
            faceIndices.add(vIdx);
            faceIndices.add(vnIdx);
            faceIndices.add(vtIdx);
        }
    }

    private static float f(String floatString) {
        if (floatString == null)
            throw new IllegalArgumentException("floatString is null");

        return Float.parseFloat(floatString);
    }
}