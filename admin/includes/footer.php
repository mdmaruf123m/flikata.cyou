</main>

<!-- Bootstrap 5 Bundle JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<script>
    // 3-Line Hamburger Sidebar Toggle Script
    function toggleSidebar() {
        const sidebar = document.getElementById('sidebar');
        const topbar = document.getElementById('topbar');
        const mainContent = document.getElementById('main-content');
        const backdrop = document.getElementById('sidebarBackdrop');

        if (window.innerWidth <= 991.98) {
            // Mobile toggle
            sidebar.classList.toggle('show-mobile');
            backdrop.classList.toggle('show');
        } else {
            // Desktop toggle
            sidebar.classList.toggle('collapsed');
            topbar.classList.toggle('expanded');
            mainContent.classList.toggle('expanded');
        }
    }

    // Helper for SweetAlert2 Toasts
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        didOpen: (toast) => {
            toast.addEventListener('mouseenter', Swal.stopTimer);
            toast.addEventListener('mouseleave', Swal.resumeTimer);
        }
    });

    // Handle session alert messages if present in URL
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('msg_success')) {
        Toast.fire({
            icon: 'success',
            title: urlParams.get('msg_success')
        });
    } else if (urlParams.has('msg_error')) {
        Toast.fire({
            icon: 'error',
            title: urlParams.get('msg_error')
        });
    }
</script>
</body>
</html>
