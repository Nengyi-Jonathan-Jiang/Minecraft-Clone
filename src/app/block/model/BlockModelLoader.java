package app.block.model;

import util.ArrayUtil;
import util.FileReader;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class BlockModelLoader {
    public static BlockModel loadModel(String filePath) {
        Scanner scan = new Scanner(FileReader.readAsStream(filePath));

        List<PartialMeshVertex> vertices = new ArrayList<>();
        List<Integer> topIndices = new ArrayList<>(),
                      frontIndices = new ArrayList<>(),
                      rightIndices = new ArrayList<>(),
                      bottomIndices = new ArrayList<>(),
                      backIndices = new ArrayList<>(),
                      leftIndices = new ArrayList<>(),
                      innerIndices = new ArrayList<>();


        while(scan.hasNextLine()) {
            String line = scan.nextLine().strip().replaceAll(" {2,}", " ");
            Scanner s = new Scanner(line);

            switch (s.hasNext() ? s.next() : "") {
                case "vertex":
                    float x, y, z, tx, ty;
                    try {
                        x = Float.parseFloat(s.next());
                        y = Float.parseFloat(s.next());
                        z = Float.parseFloat(s.next());
                        tx = Float.parseFloat(s.next());
                        ty = Float.parseFloat(s.next());
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid input in line: \"%s\"".formatted(line), e);
                    }
                    vertices.add(new PartialMeshVertex(
                        x / 16f - .5f,
                        y / 16f - .5f,
                        z / 16f - .5f,
                        tx,
                        ty
                    ));
                    break;
                case "//":
                    break;
                case "face":
                    String face = s.next();
                    List<Integer> target = switch (face) {
                        case "top" -> topIndices;
                        case "front" -> frontIndices;
                        case "right" -> rightIndices;
                        case "bottom" -> bottomIndices;
                        case "back" -> backIndices;
                        case "left" -> leftIndices;
                        case "inner" -> innerIndices;
                        default -> throw new RuntimeException("Error while reading block model: Unknown face " + face);
                    };
                    while(s.hasNextInt()) {
                        target.add(s.nextInt() - 1);
                    }
                    break;
            }
        }

        PartialMeshVertex[] verticesArr = vertices.toArray(PartialMeshVertex[]::new);

        PartialMesh top    = new PartialMesh(verticesArr, ArrayUtil.toIntArray(topIndices));
        PartialMesh front  = new PartialMesh(verticesArr, ArrayUtil.toIntArray(frontIndices));
        PartialMesh right  = new PartialMesh(verticesArr, ArrayUtil.toIntArray(rightIndices));
        PartialMesh bottom = new PartialMesh(verticesArr, ArrayUtil.toIntArray(bottomIndices));
        PartialMesh back   = new PartialMesh(verticesArr, ArrayUtil.toIntArray(backIndices));
        PartialMesh left   = new PartialMesh(verticesArr, ArrayUtil.toIntArray(leftIndices));
        PartialMesh inner  = new PartialMesh(verticesArr, ArrayUtil.toIntArray(innerIndices));

        return new BlockModel(top, front, right, bottom, back, left, inner);
    }
}
