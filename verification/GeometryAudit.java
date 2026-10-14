import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.artmapgrid.GridOverlay;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class GeometryAudit {
    public static void main(String[] args) throws Exception {
        var cross = GridOverlay.class.getDeclaredMethod("cross", PoseStack.Pose.class, VertexConsumer.class,
                float.class, float.class, float.class, float.class, int.class, int.class);
        cross.setAccessible(true);
        List<float[]> points = new ArrayList<>();
        var consumer = (VertexConsumer) Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),
                new Class<?>[]{VertexConsumer.class}, (proxy, method, values) -> {
                    if (method.getName().equals("addVertex") && values.length == 4) {
                        points.add(new float[]{(float) values[1], (float) values[2], (float) values[3]});
                    }
                    return method.getReturnType() == VertexConsumer.class ? proxy : null;
                });
        for (float x : new float[]{62, 66}) {
            for (float y : new float[]{62, 66}) {
                points.clear();
                cross.invoke(null, null, consumer, x, y, .48f, -.06f, 0xD9222529, 0);
                cross.invoke(null, null, consumer, x, y, .22f, -.065f, 0xF2F4F4F0, 0);
                if (points.size() != 16) throw new AssertionError("Incomplete X marker");
                for (float[] point : points) {
                    if (point[0] <= x - 2 || point[0] >= x + 2 || point[1] <= y - 2 || point[1] >= y + 2)
                        throw new AssertionError("Marker crosses a pixel boundary");
                }
                for (int offset = 0; offset < points.size(); offset += 4) {
                    float[] a = points.get(offset), b = points.get(offset + 1), c = points.get(offset + 2);
                    float winding = (b[0]-a[0])*(c[1]-a[1]) - (b[1]-a[1])*(c[0]-a[0]);
                    if (winding >= 0) throw new AssertionError("Marker has reversed winding");
                }
            }
        }
        System.out.println("PASS: all four outlined X markers fit strictly inside their central pixels with correct winding.");
        var sections = GridOverlay.class.getDeclaredMethod("drawSections", PoseStack.Pose.class,
                VertexConsumer.class, int.class, int.class);
        sections.setAccessible(true);
        for (int size : new int[]{2,4,8,16,32}) {
            points.clear();
            sections.invoke(null,null,consumer,size,0);
            int cells = 32/size;
            if (points.size() != (cells+1)*(cells+1)*4) throw new AssertionError("Missing section edges");
            for (float[] point : points) {
                if (point[0]<0 || point[0]>128 || point[1]<0 || point[1]>128)
                    throw new AssertionError("Section outside canvas");
            }
            for (int line = 0; line <= cells; line++) {
                int start = line*(cells+1)*4;
                float left = points.get(start)[0], right = points.get(start+1)[0];
                float expected = line*size*4;
                if (expected==0 ? left!=0 : expected==128 ? right!=128 : Math.abs((left+right)/2-expected)>.001f)
                    throw new AssertionError("Section boundary not aligned with painting pixels");
            }
        }
        for (int invalid : new int[]{0,3,5,7,12,64,-8}) {
            points.clear(); sections.invoke(null,null,consumer,invalid,0);
            if (!points.isEmpty()) throw new AssertionError("Invalid size rendered");
        }
        System.out.println("PASS: every section size evenly divides the canvas; boundaries align with pixel edges; invalid sizes rejected.");
    }
}
