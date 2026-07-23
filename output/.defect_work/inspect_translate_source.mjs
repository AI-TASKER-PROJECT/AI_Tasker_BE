import fs from "node:fs/promises";
import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const sourcePath =
  "D:/Projesct-tong/be-project/AI_Tasker_BE/output/Defect_List_Expanded.xlsx";
const workDir =
  "D:/Projesct-tong/be-project/AI_Tasker_BE/output/.defect_work";

const workbook = await SpreadsheetFile.importXlsx(
  await FileBlob.load(sourcePath),
);

const summary = await workbook.inspect({
  kind: "workbook,sheet,table",
  maxChars: 5000,
  tableMaxRows: 4,
  tableMaxCols: 6,
  tableMaxCellChars: 100,
});
console.log(summary.ndjson);

for (const [sheetName, range, fileName] of [
  ["Defect List", "A1:T18", "translate_before_defects.png"],
  ["User Story Coverage", "A1:I18", "translate_before_coverage.png"],
  ["Evidence", "A1:H24", "translate_before_evidence.png"],
]) {
  const preview = await workbook.render({
    sheetName,
    range,
    scale: 1,
    format: "png",
  });
  await fs.writeFile(
    `${workDir}/${fileName}`,
    new Uint8Array(await preview.arrayBuffer()),
  );
}

for (const [sheetName, range] of [
  ["Defect List", "A1:T69"],
  ["User Story Coverage", "A1:I75"],
  ["Evidence", "A1:H211"],
]) {
  const inspection = await workbook.inspect({
    kind: "table",
    range: `${sheetName}!${range}`,
    include: "values,formulas",
    tableMaxRows: 250,
    tableMaxCols: 20,
    tableMaxCellChars: 500,
    maxChars: 400000,
  });
  await fs.writeFile(
    `${workDir}/${sheetName.replaceAll(" ", "_")}_source.ndjson`,
    inspection.ndjson,
    "utf8",
  );
}
