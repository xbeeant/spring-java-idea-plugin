package com.xstudio.plugin.idea.sj.javadoc.entity;

import com.xstudio.plugin.idea.sj.translate.BiyingTranslate;
import com.xstudio.plugin.idea.sj.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

public class JavaDoc {
    private String description = "";

    private final List<Param> params = new ArrayList<>();
    private String rturn;
    private final List<String> sees = new ArrayList<>();
    private final List<Throws> throwz = new ArrayList<>();

    /**
     * 设置 description.
     *
     * <p>通过 getDescription() 获取 description</p>
     *
     * @param description description
     */
    public void addDescription(String description) {
        this.description += "* " + description;
    }

    public void addParam(String name, String link) {
        Param param = new Param(name, link);
        params.add(param);
    }

    public void addSee(String see) {
        sees.add(see);
    }

    public void addThrowz(String name) {
        this.throwz.add(new Throws(name));
    }

    /**
     * 设置 rturn.
     *
     * @param rturn rturn
     */
    public void setRturn(String rturn) {
        this.rturn = rturn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/**\n");
        sb.append(description);
        sb.append("\n*\n");
        for (Param param : params) {
            sb.append("* @param ");
            sb.append(param.toString());
            sb.append("\n");
        }

        if (null != rturn) {
            sb.append("* @return {@link ");
            sb.append(rturn);
            sb.append("}\n");
        }

        if (!sees.isEmpty()) {
            for (String see : sees) {
                sb.append("* @see ");
                sb.append(see);
                sb.append("\n");
            }
        }

        if (!throwz.isEmpty()) {
            for (Throws thro : throwz) {
                sb.append(thro.toString());
                sb.append("\n");
            }
        }


        sb.append("*/\n");
        return sb.toString();
    }

    public static class Param {
        private String description;

        private final String name;

        public Param(String name, String link) {
            this.name = name;
            try {
                this.description = BiyingTranslate.translate(name);
                if (null != link) {
                    this.description += " {@link " + link + "}";
                }
            } catch (Exception e) {
                this.description = name;
            }
        }

        @Override
        public String toString() {
            return name + " " + description;
        }
    }

    public static class Throws {
        private final String name;

        public Throws(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "* @throws " +
                    name +
                    " " +
                    BiyingTranslate.translate(JavaBeansUtil.humpToSpace(name));
        }
    }
}
