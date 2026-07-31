import fs from "node:fs/promises";
import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const workDir =
  "D:/Projesct-tong/be-project/AI_Tasker_BE/output/.defect_work";
const sourcePath = `${workDir}/normalized_source.xlsx`;
const outputPath =
  "D:/Projesct-tong/be-project/AI_Tasker_BE/output/Defect_List_Expanded_EN.xlsx";

const workbook = await SpreadsheetFile.importXlsx(
  await FileBlob.load(sourcePath),
);

const expectedSheets = ["Defect List", "User Story Coverage", "Evidence"];
const actualSheets = workbook.worksheets.items.map(sheet => sheet.name);
if (JSON.stringify(actualSheets) !== JSON.stringify(expectedSheets)) {
  throw new Error(`Unexpected sheets: ${actualSheets.join(", ")}`);
}

const needsTranslation = value =>
  typeof value === "string" && /[^\u0000-\u007f]/.test(value);

async function translateStrings(values) {
  const unique = [...new Set(values.filter(needsTranslation))];
  const translated = new Map();
  const chunks = [];
  let current = [];
  let currentLength = 0;

  for (const value of unique) {
    const addition = value.length + 32;
    if (current.length && currentLength + addition > 2800) {
      chunks.push(current);
      current = [];
      currentLength = 0;
    }
    current.push(value);
    currentLength += addition;
  }
  if (current.length) chunks.push(current);

  for (const chunk of chunks) {
    const marked = chunk
      .map((value, index) => `ZXQ${String(index + 1).padStart(5, "0")}ZXQ\n${value}`)
      .join("\n") + "\nZXQENDZXQ";
    const url = new URL("https://translate.googleapis.com/translate_a/single");
    url.searchParams.set("client", "gtx");
    url.searchParams.set("sl", "vi");
    url.searchParams.set("tl", "en");
    url.searchParams.set("dt", "t");
    url.searchParams.set("q", marked);

    let response;
    for (let attempt = 1; attempt <= 3; attempt++) {
      response = await fetch(url);
      if (response.ok) break;
      if (attempt === 3) {
        throw new Error(`Translation request failed: ${response.status}`);
      }
      await new Promise(resolve => setTimeout(resolve, 600 * attempt));
    }
    const payload = await response.json();
    const output = (payload[0] || []).map(part => part[0] || "").join("");

    for (let index = 0; index < chunk.length; index++) {
      const marker = `ZXQ${String(index + 1).padStart(5, "0")}ZXQ`;
      const nextMarker = index + 1 < chunk.length
        ? `ZXQ${String(index + 2).padStart(5, "0")}ZXQ`
        : "ZXQENDZXQ";
      const start = output.indexOf(marker);
      const end = output.indexOf(nextMarker, start + marker.length);
      if (start < 0 || end < 0) {
        throw new Error(`Translation marker lost: ${marker}`);
      }
      translated.set(
        chunk[index],
        output.slice(start + marker.length, end).trim(),
      );
    }
  }
  return translated;
}

const targetRanges = [
  ["Defect List", "C2:H69"],
  ["Defect List", "P2:P69"],
  ["Defect List", "R2:R69"],
  ["Defect List", "T2:T69"],
  ["User Story Coverage", "B2:D75"],
  ["User Story Coverage", "I2:I75"],
  ["Evidence", "D2:F211"],
  ["Evidence", "H2:H211"],
];

const matrices = targetRanges.map(([sheetName, address]) => {
  const range = workbook.worksheets.getItem(sheetName).getRange(address);
  return { sheetName, address, range, values: range.values };
});
const inputs = matrices.flatMap(item => item.values.flat());
const translations = await translateStrings(inputs);

for (const item of matrices) {
  const englishValues = item.values.map(row =>
    row.map(value => translations.get(value) ?? value),
  );
  item.range.values = englishValues;
}

for (const sheetName of expectedSheets) {
  const sheet = workbook.worksheets.getItem(sheetName);
  sheet.freezePanes.unfreeze();
  sheet.freezePanes.freezeRows(1);
  sheet.freezePanes.freezeColumns(1);
}

const remainingNonEnglish = [];
for (const item of matrices) {
  for (let row = 0; row < item.values.length; row++) {
    for (let col = 0; col < item.values[row].length; col++) {
      const value = item.range.values[row][col];
      if (needsTranslation(value)) {
        remainingNonEnglish.push({
          sheet: item.sheetName,
          range: item.address,
          value,
        });
      }
    }
  }
}
if (remainingNonEnglish.length) {
  throw new Error(
    `Non-English text remains in translated fields: ${JSON.stringify(remainingNonEnglish.slice(0, 10))}`,
  );
}

const formulaErrors = await workbook.inspect({
  kind: "match",
  searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A",
  options: { useRegex: true, maxResults: 300 },
  summary: "translated workbook formula error scan",
});
await fs.writeFile(
  `${workDir}/translate_formula_errors.ndjson`,
  formulaErrors.ndjson,
  "utf8",
);

for (const [sheetName, range, fileName] of [
  ["Defect List", "A1:T18", "translate_after_defects.png"],
  ["User Story Coverage", "A1:I18", "translate_after_coverage.png"],
  ["Evidence", "A1:H24", "translate_after_evidence.png"],
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

const output = await SpreadsheetFile.exportXlsx(workbook);
await output.save(outputPath);

const verification = await SpreadsheetFile.importXlsx(
  await FileBlob.load(outputPath),
);
const verificationSheets = verification.worksheets.items.map(sheet => sheet.name);
const qa = {
  outputPath,
  sheets: verificationSheets,
  rows: Object.fromEntries(
    expectedSheets.map(name => [
      name,
      verification.worksheets.getItem(name).getUsedRange().rowCount,
    ]),
  ),
  translatedRanges: targetRanges,
  translatedUniqueStrings: translations.size,
};
await fs.writeFile(
  `${workDir}/translate_qa.json`,
  JSON.stringify(qa, null, 2),
  "utf8",
);
console.log(JSON.stringify(qa));
