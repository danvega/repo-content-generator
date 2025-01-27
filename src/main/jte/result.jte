@param dev.danvega.cg.ContentGenerationResponse response

<div class="bg-gray-50 rounded-lg border border-gray-200 p-4">
    <div class="relative">
        <div class="flex gap-4 mb-4">
            <div class="flex-1 bg-white p-3 rounded-lg border border-gray-200">
                <div class="text-sm text-gray-600">Bytes</div>
                <div class="text-lg font-semibold">${response.byteCount()}</div>
            </div>
            <div class="flex-1 bg-white p-3 rounded-lg border border-gray-200">
                <div class="text-sm text-gray-600">Tokens</div>
                <div class="text-lg font-semibold">${response.tokenCount()}</div>
            </div>
        </div>
        <textarea
                id="result"
                class="w-full h-96 p-4 font-mono text-sm bg-white rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                readonly
        >${response.content()}</textarea>

        <button
                onclick="copyContent()"
                class="mt-4 inline-flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg transition-colors duration-200"
        >
            Copy Content
        </button>
    </div>
</div>

<script>
    function copyContent() {
        const textarea = document.getElementById('result');
        textarea.select();
        document.execCommand('copy');

        // Show feedback
        const button = event.target;
        const originalText = button.textContent;
        button.textContent = 'Copied!';

        setTimeout(() => {
            button.textContent = originalText;
        }, 2000);
    }
</script>