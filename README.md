http-resetfull-utils
====================
我创建该项目的目的时为了方便web rest接口做单元，而编写的。

实例代码：

Map<String, String> heads = Maps.newLinkedHashMap();

heads.put("appid", "K4da4b49983cc1u009");

HttpExecutor executor = new HttpExecutor(new DefaultMessageFilter(), heads);

Map<String, Object> params = new LinkedHashMap<String, Object>();

params.put("id", "1");

params.put("name", "李斯");

params.put("phone", "13988778766");

executor.doPost("http://127.0.0.1:8080/demo/user/contact", params, new TextPrintMessageResolve());
