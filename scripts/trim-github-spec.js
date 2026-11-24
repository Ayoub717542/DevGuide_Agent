const fs = require("fs");
const url = "https://raw.githubusercontent.com/github/rest-api-description/main/descriptions/api.github.com/api.github.com.2026-03-10.json";
const TAGS = ["repos", "issues", "pulls"];

async function main() {
  const spec = await (await fetch(url)).json();
  const result = [];
  for (const [path, item] of Object.entries(spec.paths)) {
    const get = item.get;
    if (!get || !TAGS.includes(get.tags?.[0])) continue;
    const params = [...(item.parameters || []), ...(get.parameters || [])]
      .map(p => p.$ref ? spec.components.parameters[p.$ref.split("/").pop()] : p)
      .map(p => p.name);
    result.push({ path, summary: get.summary, params });
  }
  fs.mkdirSync("DevTutor/src/main/resources/github", { recursive: true });
  fs.writeFileSync("DevTutor/src/main/resources/github/github-api-get.json", JSON.stringify(result, null, 1));
  console.log(result.length + " endpoints saved");
}
main();
