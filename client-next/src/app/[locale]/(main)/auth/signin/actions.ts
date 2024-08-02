"use server";

export async function logError(tag: string, err: any) {
  console.log(`${tag} : ${err} `);
}
