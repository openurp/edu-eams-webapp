[#ftl]
[#if !(request.getHeader('x-requested-with')??) && !Parameters['x-requested-with']??]
  <script src="${base}/static/themes/admin/js/custom.js"></script>
</body>
</html>
[/#if]