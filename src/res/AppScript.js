window.bridgeReady = false;

// Initialize MathJax
window.MathJax = {
    tex: {
        inlineMath: [['$', '$'], ['\\(', '\\)']],
        displayMsath: [['$$', '$$'], ['\\[', '\\]']]
    },
    options: {
        skipHtmlTags: ['script', 'noscipt', 'style', 'textarea', 'pre', 'code']
    }
};
let tempBox = document.createElement("div");



function onBridgeReady() {
    window.bridgeReady = true;
    console.log("Bridge is now ready");
}



function appendToLastMessage(markdownText) {
    const chatbox = document.getElementById('chatbox');
    if (!document.getElementById("tempbox")) {
        tempBox = document.createElement("div");
        tempBox.id = "tempbox";
        tempBox.className = "message bot";
        chatbox.appendChild(tempBox);

    }
    tempBox.innerHTML += markdownText; //marked.parseInline(markdownText);
    window.scrollTo(0, document.body.scrollHeight);
}

function cleanTempBox(){
    const tempBoxElement = document.getElementById("tempbox");
    if(tempBoxElement){
        tempBoxElement.remove();
        tempBox = null; // Add this line
    }
}


// 
function renderMarkdown(markdownText, user) {
    // Extract mermaid code blocks
    // Trigger MathJax rendering
    if (typeof MathJax !== 'undefined' && MathJax.Hub) {
        MathJax.typeset();
        MathJax.Hub.Queue(["Typeset", MathJax.Hub, div]);
    }

    const mermaidBlocks = [];
    markdownText = markdownText.replace(/```mermaid\s+([\s\S]*?)```/g, (match, code) => {
        mermaidBlocks.push(code.trim());
        return `<!--MERMAID_BLOCK_${mermaidBlocks.length - 1}-->`;
    });

    // Parse the rest of the markdown
    let htmlContent = marked.parse(markdownText);

    // Replace placeholders with mermaid divs
    mermaidBlocks.forEach((code, index) => {
        const mermaidDiv = `<div class="mermaid">${code}</div>`;
        htmlContent = htmlContent.replace(`<!--MERMAID_BLOCK_${index}-->`, mermaidDiv);
    });


    // Create a message container div
    var div = document.createElement('div');
    div.className = 'message ' + user;
    div.innerHTML = htmlContent;



    // Append to chatbox
    document.getElementById('chatbox').appendChild(div);

    // Scroll to bottom
    window.scrollTo(0, document.body.scrollHeight);

    // Trigger MathJax rendering
    if (typeof MathJax !== 'undefined' && MathJax.Hub) {
        MathJax.typeset();
        MathJax.Hub.Queue(["Typeset", MathJax.Hub, div]);
    }

    // Trigger Mermaid rendering and add Save buttons
    if (typeof mermaid !== 'undefined') {
        const mermaidElements = div.querySelectorAll(".mermaid");
        mermaid.init(undefined, mermaidElements);

        // Add save buttons for each rendered mermaid diagram
        mermaidElements.forEach((el, index) => {
            const btn = document.createElement("button");
            // Create PNG button
            const pngBtn = document.createElement("button");
            pngBtn.textContent = "💾 Save as PNG";
            pngBtn.className = "save-btn";
            pngBtn.style.marginRight = "10px";
            pngBtn.onclick = () => saveMermaidAsPNG(el, `diagram-${index}`);

            // Create SVG button
            const svgBtn = document.createElement("button");
            svgBtn.textContent = "🖼️ Save as SVG";
            svgBtn.className = "save-btn";
            svgBtn.onclick = () => saveMermaidAsSVG(el, `diagram-${index}`);

            // Add buttons after the diagram
            el.insertAdjacentElement('afterend', svgBtn);
            el.insertAdjacentElement('afterend', pngBtn);

        });
    }

}

function renderInput() {
    const input = document.getElementById('markdownInput').value;
    renderMarkdown(input, 'user');
}



function saveMermaidAsSVG(container, filename) {
    const svg = container.querySelector('svg');
    if (!svg) {
        alert("No SVG found");
        return;
    }
    const svgData = new XMLSerializer().serializeToString(svg);
    if (window.javaApp && window.javaApp.saveFile) {
        window.javaApp.saveFile(svgData, filename + ".svg");
    } else {
        alert("Java bridge not found");
    }
}


function saveMermaidAsPNG(container, filename) {
    const svg = container.querySelector('svg');
    const svgData = new XMLSerializer().serializeToString(svg);
    const svgBlob = new Blob([svgData], {type: "image/svg+xml;charset=utf-8"});
    const reader = new FileReader();

    reader.onload = function () {
        const img = new Image();
        img.onload = function () {
            const canvas = document.createElement("canvas");
            canvas.width = img.width * 2;
            canvas.height = img.height * 2;
            const ctx = canvas.getContext("2d");
            ctx.setTransform(2, 0, 0, 2, 0, 0);
            ctx.drawImage(img, 0, 0);
            const pngData = canvas.toDataURL("image/png");
            window.javaApp.saveFile(pngData, filename + ".png");
        };
        img.src = reader.result;
    };

    reader.readAsDataURL(svgBlob);
}
