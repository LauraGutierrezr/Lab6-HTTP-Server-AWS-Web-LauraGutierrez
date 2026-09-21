// Asynchronous browser client for the hardcoded services exposed by the
// Java server. Every action here uses fetch() and updates only the result
// or error area of the page - the page itself never reloads.

const resultArea = document.getElementById("result-area");
const errorArea = document.getElementById("error-area");

const greetForm = document.getElementById("greet-form");
const greetNameInput = document.getElementById("greet-name");

const squareForm = document.getElementById("square-form");
const squareValueInput = document.getElementById("square-value");

const timeButton = document.getElementById("time-button");

/** Clears both the result and error areas before a new request starts. */
function clearOutputs() {
  errorArea.classList.remove("visible");
  errorArea.textContent = "";
}

function showResult(text) {
  resultArea.textContent = text;
}

/** A message meant for a person, never the raw exception or stack trace. */
function showError(message) {
  errorArea.textContent = message;
  errorArea.classList.add("visible");
}

function setLoading(button, isLoading, loadingLabel, idleLabel) {
  button.disabled = isLoading;
  button.textContent = isLoading ? loadingLabel : idleLabel;
}

/**
 * Fetches a hardcoded service URL and returns the parsed JSON body.
 * HTTP-level errors (4xx/5xx) and network failures (offline, DNS, CORS,
 * connection reset) are surfaced as two different, friendly messages.
 */
async function callService(url) {
  let response;
  try {
    response = await fetch(url, { method: "GET" });
  } catch (networkError) {
    // fetch() itself rejects only for network-level failures, never for
    // HTTP error statuses.
    throw new Error("Network error: could not reach the server. Check your connection and try again.");
  }

  let body;
  try {
    body = await response.json();
  } catch (parseError) {
    throw new Error("The server response could not be understood.");
  }

  if (!response.ok) {
    const serverMessage = body && body.error ? body.error : `Request failed with status ${response.status}.`;
    throw new Error(serverMessage);
  }

  return body;
}

greetForm.addEventListener("submit", async (event) => {
  event.preventDefault(); // stay on the same page
  clearOutputs();

  const name = greetNameInput.value.trim();
  if (!name) {
    showError("Please enter a name before requesting a greeting.");
    return;
  }

  const submitButton = greetForm.querySelector("button");
  setLoading(submitButton, true, "Greeting...", "Greet me");
  try {
    const url = "/api/greet?name=" + encodeURIComponent(name);
    const data = await callService(url);
    showResult(data.message);
  } catch (err) {
    showError(err.message);
  } finally {
    setLoading(submitButton, false, "Greeting...", "Greet me");
  }
});

squareForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  clearOutputs();

  const rawValue = squareValueInput.value.trim();
  if (rawValue === "" || Number.isNaN(Number(rawValue))) {
    showError("Please enter a valid number before requesting its square.");
    return;
  }

  const submitButton = squareForm.querySelector("button");
  setLoading(submitButton, true, "Calculating...", "Square it");
  try {
    const url = "/api/square?value=" + encodeURIComponent(rawValue);
    const data = await callService(url);
    showResult(data.input + " squared is " + data.square);
  } catch (err) {
    showError(err.message);
  } finally {
    setLoading(submitButton, false, "Calculating...", "Square it");
  }
});

timeButton.addEventListener("click", async () => {
  clearOutputs();
  setLoading(timeButton, true, "Fetching...", "Get server time");
  try {
    const data = await callService("/api/time");
    showResult("Server time: " + data.serverTime);
  } catch (err) {
    showError(err.message);
  } finally {
    setLoading(timeButton, false, "Fetching...", "Get server time");
  }
});
